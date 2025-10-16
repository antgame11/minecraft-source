# Super bad python script!
import os
import requests

url = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
response = requests.get(url)
data = response.json()

latestrelease = data["latest"]["release"]

os.system("git clone https://github.com/FabricMC/yarn")

os.system("cd yarn")

os.system(f"cd yarn && git checkout {latestrelease}")

os.system("cd yarn && ./gradlew mapNamedJar decompileCFR")

os.system("mv yarn/build/namedSrc ../src")