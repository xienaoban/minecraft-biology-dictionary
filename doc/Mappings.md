# The Mapping Files to Use

The two most popular mappings are Mojang's official mappings Yarn mappings currently. There are many discussions about which mapping to use:

- [Forge: It's OK to use Mojang mappings since the people at Mojang want this to be allowed.](https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md)

- [Fabric: Use Yarn mappings by default and use Mojang mappings at your own risk.](https://fabricmc.net/wiki/tutorial:mappings)

- [Architectury: Use Mojang mappings by default.](https://docs.architectury.dev/docs/forge_loom/)

- [Caffeine: They are going to switch from Yarn to Mojang mappings.](https://github.com/CaffeineMC/caffeine-meta/issues/4)

As far as I know:

- Yes Mojang mappings have a more restrictive license than Yarn. But @jellysquid3 is right:

  > In the event that Mojang does decide to revoke that good faith and start weaponizing it against us, well -- I wouldn't want to continue modding their game, anyways. ([link](https://github.com/CaffeineMC/caffeine-meta/issues/4))

  So maybe we don't have to care too much about the license.

- Mojang mappings provide names of all classes, methods and fields, but they don't provide names of parameters and local variables.
  
  Yarn mappings provide names of parameters and local variables, but their coverage is incomplete (especially lack of rendering names).

- Sometimes Yarn's names are even more elegant or reasonable in my opinion.

In summary, I think part of the reason for the controversy is that Mojang didn't release their official mappings until 1.17. It's too new to be fully accepted by the community.

So I choose to use Mojang mappings this time.