# Super bad python script!
import os
import requests
import sys

url = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
response = requests.get(url)
data = response.json()

latestrelease = data["latest"]["release"]

# when the bruzz talm bout some enter directory with os but you js know bout some os.system :rofl:

os.system(f"git switch {latestrelease} || git switch -c {latestrelease}")

os.system("git clone https://github.com/FabricMC/yarn")

switchbranch = os.system(f"cd yarn && git switch {latestrelease}")

if switchbranch != 0:
    sys.exit()

os.system("cd yarn && ./gradlew mapNamedJar decompileCFR")

os.system("rm -rf src")

os.system("mv yarn/build/namedSrc ./src")

os.system(f"""
        git config --global user.name "github-actions"
        git config --global user.email "actions@github.com"
        git add .
        git diff --cached --quiet && echo "No changes to commit." || \
        (git commit -m "Mappings Update, {latestrelease}" && git push)
          """)