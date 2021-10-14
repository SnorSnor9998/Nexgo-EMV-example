package com.snor.nexgo_emv_example.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.nexgo.oaf.apiv3.emv.AidEntity
import com.nexgo.oaf.apiv3.emv.CapkEntity
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayList

class EmvUtils(private val context : Context) {

    fun getAidList(): List<AidEntity>? {
        val aidEntityList: MutableList<AidEntity> = ArrayList()
        val ja = JsonParser.parseString(readAssetsTxt("inbas_aid.json")).asJsonArray ?: return null
        ja.forEach {
            val userBean = Gson().fromJson(it, AidEntity::class.java)
            aidEntityList.add(userBean)
        }
        return aidEntityList
    }

    fun getCapkList(): List<CapkEntity>? {
        val capkEntityList: MutableList<CapkEntity> = ArrayList()
        val ja = JsonParser.parseString(readAssetsTxt("inbas_capk.json")).asJsonArray ?: return null
        ja.forEach {
            val userBean = Gson().fromJson(it, CapkEntity::class.java)
            capkEntityList.add(userBean)
        }
        return capkEntityList
    }


    private fun readAssetsTxt(fileName: String): String? {
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size = inputStream.available()
            // Read the entire asset into a local byte buffer.
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            // Convert the buffer into a string.
            return String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}