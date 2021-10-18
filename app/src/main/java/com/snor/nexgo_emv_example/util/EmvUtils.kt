package com.snor.nexgo_emv_example.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.nexgo.libpboc.ByteUtils
import com.nexgo.oaf.apiv3.emv.AidEntity
import com.nexgo.oaf.apiv3.emv.CapkEntity
import com.nexgo.oaf.apiv3.emv.EmvHandler2
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayList

class EmvUtils(private val emvHandler: EmvHandler2, private val context: Context) {

    fun init(){
        initEmvAid()
        initEmvCapk()
        initTerminalConfig()
    }

    private fun initEmvAid(){
        emvHandler.delAllAid()

        val aidEntityList: MutableList<AidEntity> = ArrayList()
        val ja = JsonParser.parseString(readAssetsTxt("inbas_aid.json")).asJsonArray
        ja.forEach {
            val userBean = Gson().fromJson(it, AidEntity::class.java)
            aidEntityList.add(userBean)
        }

        if (aidEntityList.isNullOrEmpty()) {
            Log.e("dd--", "initAID failed")
            return
        }
        emvHandler.setAidParaList(aidEntityList)
    }



    private fun initEmvCapk() {
        emvHandler.delAllCapk()

        val capkEntityList: MutableList<CapkEntity> = ArrayList()
        val ja = JsonParser.parseString(readAssetsTxt("inbas_capk.json")).asJsonArray
        ja.forEach {
            val userBean = Gson().fromJson(it, CapkEntity::class.java)
            capkEntityList.add(userBean)
        }

        if (capkEntityList.isNullOrEmpty()) {
            Log.e("dd--", "initCAPK failed")
            return
        }
        emvHandler.setCAPKList(capkEntityList)
    }


    private fun initTerminalConfig(){
        Log.e("dd--","initTerminalConfig")
        //9F1A, 5F2A, 9F3C, 9F33
        val b : ByteArray = ByteUtils.hexString2ByteArray("9F1A0204585F2A0204589F3C0204589F3303E0F8C8")
        emvHandler.initTermConfig(b)

        //Terminal Capabilities
        var tag: ByteArray = ByteUtils.hexString2ByteArray("9F33")
        var value: ByteArray = ByteUtils.hexString2ByteArray("E060C8")
        emvHandler.setTlv(tag,value)

        //Terminal Transaction Qualifiers (TTQ)
        tag = ByteUtils.hexString2ByteArray("9F66")
        value = ByteUtils.hexString2ByteArray("3600C000")
        emvHandler.setTlv(tag,value)

        //Additional Terminal Capabilities (ATC)
        tag = ByteUtils.hexString2ByteArray("9F40")
        value = ByteUtils.hexString2ByteArray("6100D0B001")
        emvHandler.setTlv(tag,value)

        //PIN Try Limit
        tag = ByteUtils.hexString2ByteArray("DF03")
        value = ByteUtils.hexString2ByteArray("FA03")
        emvHandler.setTlv(tag,value)

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