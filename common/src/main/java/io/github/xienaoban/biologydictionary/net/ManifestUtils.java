package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * The manifest of this build: what version it is and what it speaks, plus the check that compares
 * two of them.
 *
 * <p>The manifest travels as a single YAML string instead of typed packet fields, so that keys can
 * be added later without touching the codec. Reading it is lenient: unknown keys are ignored and
 * missing keys mean "unknown", unlike the config file parser.</p>
 *
 * <p>Whether two builds are compatible is decided by the packet ids and
 * {@link #PROTOCOL_VERSION}; the mod version is only printed to players and logs. A release that
 * changes nothing but texts therefore stays silent, which is the whole point.</p>
 *
 * <p>A server that never sends a manifest is simply left alone. The loaders expose no portable way
 * to ask what a remote peer runs, and the only common signal would be a timeout, which cannot tell
 * a legacy server from a vanilla one.</p>
 */
public final class ManifestUtils {
    /**
     * Bumped by hand whenever the wire format of an existing packet changes.
     *
     * <p>Renaming, adding or removing a packet is caught by the packet id list automatically; this
     * constant covers the case where the ids stay the same but their fields do not.</p>
     */
    public static final int PROTOCOL_VERSION = 1;

    private static final String KEY_MOD_VERSION = "mod_version";
    private static final String KEY_PROTOCOL_VERSION = "protocol_version";
    private static final String KEY_PACKETS = "packets";

    /**
     * Loading and dumping are not thread safe, and a single player world can do both at once
     * (the server sends on login while the client reads on the network thread), so each keeps
     * its own instance.
     */
    private static final Yaml LOADER = new Yaml(new SafeConstructor(new LoaderOptions()));
    private static final Yaml DUMPER = new Yaml();

    private ManifestUtils() {}

    /**
     * Builds the manifest of this build as YAML.
     */
    public static String build() {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put(KEY_MOD_VERSION, DevUtils.getModVersion());
        manifest.put(KEY_PROTOCOL_VERSION, PROTOCOL_VERSION);
        manifest.put(KEY_PACKETS, packetIds());
        return DUMPER.dump(manifest);
    }

    /**
     * Compares the server's manifest with our own and tells the player when they do not match.
     *
     * <p>An unreadable manifest counts as a mismatch: the document is tiny and written by the same
     * code, so failing to read it means the other side is not what we expect.</p>
     */
    @ClientOnly
    public static void receive(String manifestYaml) {
        Manifest server = null;
        try {
            server = parse(manifestYaml);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to read the server manifest", e);
        }

        if (server != null) {
            Set<String> localPackets = new HashSet<>(packetIds());
            if (server.protocolVersion() == PROTOCOL_VERSION && localPackets.equals(server.packets())) {
                LOGGER.debug("Server manifest matches, server version is {}", server.modVersion());
                return;
            }

            Set<String> missingOnServer = new LinkedHashSet<>(localPackets);
            missingOnServer.removeAll(server.packets());
            Set<String> unknownToUs = new LinkedHashSet<>(server.packets());
            unknownToUs.removeAll(localPackets);
            LOGGER.warn("Mod compatibility mismatch: client {} / server {}, protocol {}/{}, "
                            + "packets missing on the server {}, packets unknown to the client {}",
                    DevUtils.getModVersion(), server.modVersion(), PROTOCOL_VERSION, server.protocolVersion(),
                    missingOnServer, unknownToUs);
        } else {
            LOGGER.warn("Server manifest is unreadable, treating it as incompatible");
        }

        String serverVersion = server == null ? "" : server.modVersion();
        if (serverVersion.isEmpty()) {
            serverVersion = "?";
        }
        ClientUtils.sendTextBoxMessage(TextUtils.modLog(TextUtils.translate(
                Lang.TEXT_SERVER_MANIFEST_MISMATCH, DevUtils.getModVersion(), serverVersion)));
    }

    /**
     * Reads a manifest, treating anything unexpected as unknown.
     *
     * @return the manifest, or null when the document is not a mapping
     */
    private static Manifest parse(String manifestYaml) {
        if (!(LOADER.load(manifestYaml) instanceof Map<?, ?> map)) {
            return null;
        }
        String modVersion = Objects.toString(map.get(KEY_MOD_VERSION), "");
        int protocolVersion = map.get(KEY_PROTOCOL_VERSION) instanceof Number number
                ? number.intValue() : Integer.MIN_VALUE;
        return new Manifest(modVersion, protocolVersion, toStringSet(map.get(KEY_PACKETS)));
    }

    /**
     * The ids of every payload this build knows, sorted.
     *
     * <p>Ids are derived from the packet class names, so a rename shows up as a changed id.</p>
     */
    private static List<String> packetIds() {
        List<String> ids = new ArrayList<>(PacketPayloads.ENTRIES.size());
        for (PacketPayloads.Entry<?> entry : PacketPayloads.ENTRIES) {
            ids.add(PacketUtil.getType(entry.packetClass()).id().toString());
        }
        ids.sort(null);
        return ids;
    }

    private static Set<String> toStringSet(Object value) {
        Set<String> set = new HashSet<>();
        if (value instanceof Iterable<?> list) {
            for (Object element : list) {
                if (element != null) {
                    set.add(element.toString());
                }
            }
        }
        return set;
    }

    private record Manifest(String modVersion, int protocolVersion, Set<String> packets) {}
}
