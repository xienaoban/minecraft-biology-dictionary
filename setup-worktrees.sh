#!/usr/bin/env bash
#
# Expand the per-MC-version worktrees of this repository.
#
# Every directory in WORKTREES below becomes a linked worktree of the matching branch, so all
# versions share one object database: no duplicated clones, and cross-version commands like
# `git diff main-26.3 main-26.2 -- <path>` work locally.
#
# Usage:
#   bash setup-worktrees.sh            create missing worktrees, existing directories are skipped
#   bash setup-worktrees.sh --no-lock  same, but do not lock the created worktrees
#   bash setup-worktrees.sh fix        rewrite the .git files of existing worktrees
#
# Run it in a fresh `git clone -b tool` checkout. Two worktree quirks are handled for the current
# WSL + Windows dual-OS setup:
#
#   1. `git worktree add` writes an absolute gitdir into <dir>/.git, which only one side can resolve
#      (WSL git reads /mnt/..., Git for Windows reads E:/...). It is rewritten to a relative path,
#      which both sides resolve.
#   2. The reverse pointer (<main>/.git/worktrees/<dir>/gitdir) must be absolute, so it is only valid
#      on the side that created the worktree. The other side then sees the worktree as "prunable" and
#      `git worktree prune` there would drop its administrative files, so created worktrees are
#      locked. Unlock (`git worktree unlock <dir>`) before removing or moving one.
#      On a single-OS machine the lock is not needed, hence --no-lock; a relative gitdir is harmless
#      either way.
#
set -u

# Directory <-> branch table; keep in sync with the version table in AGENTS.md.
WORKTREES="
minecraft-biology-dictionary-26.3:main-26.3
minecraft-biology-dictionary-26.2:main-26.2
minecraft-biology-dictionary-26.1.2:main-26.1.2
minecraft-biology-dictionary-architectury-1.21.11:main-architectury-1.21.11
minecraft-biology-dictionary-architectury-1.21.1:main-architectury-1.21.1
minecraft-biology-dictionary-architectury-1.20.1:main-architectury-1.20.1
"

usage() {
  cat << 'EOF'
Usage:
  bash setup-worktrees.sh            create missing worktrees, existing directories are skipped
  bash setup-worktrees.sh --no-lock  same, but do not lock the created worktrees
  bash setup-worktrees.sh fix        rewrite the .git files of existing worktrees
EOF
}

# The worktree administration directory is named after the basename of the worktree path, so the
# relative gitdir is always ../.git/worktrees/<dir> for the direct children below.
write_gitfile() {
  printf 'gitdir: ../.git/worktrees/%s\n' "$1" > "$1/.git"
}

create_worktrees() {
  git fetch --all --prune || echo "warning: git fetch failed, using the refs already present" >&2
  for entry in $WORKTREES; do
    dir=${entry%%:*}
    branch=${entry##*:}
    if [ -e "$dir" ]; then
      echo "skip: $dir already exists"
      continue
    fi
    if ! git rev-parse --verify --quiet "refs/remotes/origin/$branch" > /dev/null; then
      echo "error: origin/$branch is missing, run: git remote set-branches origin '*' && git fetch origin" >&2
      continue
    fi
    if ! git worktree add "$dir" "$branch"; then
      echo "error: git worktree add failed for $dir" >&2
      continue
    fi
    if [ ! -d ".git/worktrees/$dir" ]; then
      echo "error: unexpected worktree administration directory .git/worktrees/$dir" >&2
      continue
    fi
    if [ "$lock" -eq 1 ]; then
      git worktree lock --reason "cross-OS worktree" "$dir" || continue
    fi
    write_gitfile "$dir"
    echo "created: $dir -> $branch"
  done
  echo "done. existing worktrees are left untouched; unlock one to remove or move it."
}

fix_gitfiles() {
  for entry in $WORKTREES; do
    dir=${entry%%:*}
    if [ -d ".git/worktrees/$dir" ]; then
      write_gitfile "$dir"
      echo "fixed: $dir/.git"
    fi
  done
}

cd "$(dirname "$0")" || exit 1

lock=1
command=create
for arg in "$@"; do
  case "$arg" in
    --no-lock) lock=0 ;;
    fix) command=fix ;;
    -h|--help) usage; exit 0 ;;
    *) echo "unknown argument: $arg" >&2; usage >&2; exit 2 ;;
  esac
done

case "$command" in
  fix) fix_gitfiles ;;
  *) create_worktrees ;;
esac
