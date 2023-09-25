import os

client_paths = [
    "src/main/java/io/github/xienaoban/minecraft/biologydictionary/gui",
    "src/main/java/io/github/xienaoban/minecraft/biologydictionary/client",
    "src/main/java/io/github/xienaoban/minecraft/biologydictionary/platform/client",
    "src/main/java/io/github/xienaoban/minecraft/biologydictionary/platform/gui",
]

def dfs_require_dfs(cur_path: str):
    for c in sorted(os.listdir(cur_path)):
        p = os.path.join(cur_path, c)
        if os.path.isfile(p):
            if p.endswith('.java'):
                with open(p) as f:
                    l = len([line for line in f.readlines() if "@Environment(EnvType.CLIENT)" in line])
                if l == 0:
                    print(p)
        elif os.path.isdir(p):
            dfs_require_dfs(p)

if __name__ == "__main__":
    for p in client_paths:
        dfs_require_dfs(os.path.join("..", p))
