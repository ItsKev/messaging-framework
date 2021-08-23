rootProject.name = "messaging-framework"

include("common")
include("bukkit")
include("proxy")

project(":common").name = "messaging-framework-common"
project(":proxy").name = "messaging-framework-proxy"
project(":bukkit").name = "messaging-framework-bukkit"
