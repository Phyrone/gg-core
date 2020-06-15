package de.phyrone.gg.module

import de.phyrone.gg.GGApi

interface GGApiProvider<T, A : GGApi> {
    fun getApi(target: T): A

}