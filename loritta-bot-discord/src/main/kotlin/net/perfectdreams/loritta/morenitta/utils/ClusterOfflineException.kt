package net.perfectdreams.loritta.morenitta.utils

data class ClusterOfflineException(val id: Int, val name: String) : RuntimeException("Cluster $id ($name) is offline")