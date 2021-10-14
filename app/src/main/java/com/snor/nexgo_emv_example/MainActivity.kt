package com.snor.nexgo_emv_example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import com.google.gson.Gson
import com.nexgo.common.ByteUtils
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.SdkResult
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum
import com.nexgo.oaf.apiv3.device.pinpad.PinPad
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum
import com.nexgo.oaf.apiv3.emv.*
import com.snor.nexgo_emv_example.Callback.CheckCardCallback
import com.snor.nexgo_emv_example.Callback.EMVCallback2
import com.snor.nexgo_emv_example.Callback.PinPadCallback
import com.snor.nexgo_emv_example.databinding.ActivityMainBinding
import com.snor.nexgo_emv_example.util.EmvUtils
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import com.nexgo.oaf.apiv3.device.pinpad.PinKeyboardModeEnum


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding()

    private var deviceEngine: DeviceEngine? = null
    private var emvHandler: EmvHandler2? = null
    private var sdkPinPad: PinPad? = null

    private var mExistSlot: CardSlotTypeEnum? = null
    private var mCardNo: String = ""
    private var mCardType = 0
    private var mPinType: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceEngine = APIProxy.getDeviceEngine(this)
        emvHandler = deviceEngine!!.getEmvHandler2("app2")
        sdkPinPad = deviceEngine!!.pinPad
        emvHandler!!.emvDebugLog(true)

        initEmvAid()
        initEmvCapk()

        binding.btnStart.setOnClickListener {
            checkCard()
        }


    }

    private fun initEmvAid() {
        emvHandler!!.delAllAid()
        val list = EmvUtils(this).getAidList()
        if (list.isNullOrEmpty()) {
            Log.e("dd--", "initAID failed")
            return
        }
        emvHandler!!.setAidParaList(list)
    }

    private fun initEmvCapk() {
        emvHandler!!.delAllCapk()
        val list = EmvUtils(this).getAidList()
        if (list.isNullOrEmpty()) {
            Log.e("dd--", "initCAPK failed")
            return
        }
        emvHandler!!.setCAPKList(list)
    }


    private fun checkCard() {
        Log.e("dd--", "_startEMVTest")

        //Init the cardReader object by retrieving an instance from the deviceEngine
        val cardReader = deviceEngine!!.cardReader

        //Create HashSet of types of capture methods we are listening for
        val slotTypes = HashSet<CardSlotTypeEnum>()
        slotTypes.add(CardSlotTypeEnum.ICC1)
//        slotTypes.add(CardSlotTypeEnum.SWIPE)
        slotTypes.add(CardSlotTypeEnum.RF)

        //Call the searchCard method (HashSet of capture types, listen timeout in seconds, which onCardInfoListener callback to use)
        cardReader.searchCard(slotTypes, 60, mCheckCardCallback)
    }


    private val mCheckCardCallback = object : CheckCardCallback() {
        override fun onCardInfo(p0: Int, p1: CardInfoEntity?) {
            super.onCardInfo(p0, p1)
            Log.e("dd--", "Enter onCardInfo retCode: $p0")


            //Check if return code is success, and the returned cardInfo object is not null
            if (p0 == SdkResult.Success && p1 != null) {

                mExistSlot = p1.cardExistslot

                if (p1.cardExistslot == CardSlotTypeEnum.RF) {
                    val m1 = deviceEngine!!.m1CardHandler
                    val uid = m1.readUid()
                    Log.e("dd--", "UID : $uid")
                    Log.e("dd--", "RF Type : ${p1.rfCardType}")
                }



                //success and cardInfo is not null
                val transData = EmvTransConfigurationEntity()
                transData.transAmount = leftPad("100", 12, '0')
                transData.termId = "00000001" //Set the terminalid
                transData.merId = "000000000000001" //Set the merchantid
                transData.emvTransType = 0x00.toByte()
                transData.isContactForceOnline = false
                transData.countryCode = "458"
                transData.currencyCode = "458"

                transData.transDate =
                    SimpleDateFormat(
                        "yyMMdd",
                        Locale.getDefault()
                    ).format(Date()) //set the trans date
                transData.transTime =
                    SimpleDateFormat(
                        "hhmmss",
                        Locale.getDefault()
                    ).format(Date()) //set the trans time
                transData.traceNo = "00000000" //set the traceno

                transData.emvProcessFlowEnum = EmvProcessFlowEnum.EMV_PROCESS_FLOW_STANDARD
                if (p1.cardExistslot == CardSlotTypeEnum.RF) {
                    transData.emvEntryModeEnum = EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACTLESS
                } else {
                    transData.emvEntryModeEnum = EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACT
                }


                //Make the call to begin the actual EMV process
                emvHandler!!.emvProcess(transData, mEMVCallback)

            }


        }
    }



    private val mEMVCallback = object : EMVCallback2() {

        override fun onTransInitBeforeGPO() {
            super.onTransInitBeforeGPO()

            val raw: ByteArray =
                emvHandler!!.getTlv(byteArrayOf(0x4F), EmvDataSourceEnum.FROM_KERNEL)
            val aid = ByteUtils.byteArray2HexString(raw)
            Log.e("dd--","aid: $aid")

            if (mExistSlot == CardSlotTypeEnum.RF) {
                if (raw.isNotEmpty()) {
                    val isVisa = aid.startsWith("A000000003")
                    val isMaster = (aid.startsWith("A000000004") || aid.startsWith("A000000005"))

                    if (isVisa){
                        // VISA(PayWave)
                        Log.e("dd--", "detect VISA card")
                    }else if(isMaster){
                        // MasterCard(PayPass)
                        Log.e("dd--", "detect MasterCard card")
                        configPaypassParameter(raw)
                    }


                }
            }

            emvHandler!!.onSetTransInitBeforeGPOResponse(true)

        }

        override fun onSelApp(
            p0: MutableList<String>?,
            p1: MutableList<CandidateAppInfoEntity>?,
            p2: Boolean
        ) {
            super.onSelApp(p0, p1, p2)

            Log.e("dd--", "onAppSelect isFirstSelect:$p2")

            p1?.forEach {
                Log.e("dd--", "EMVCandidate:$it")
            }

        }

        override fun onPrompt(p0: PromptEnum?) {
            super.onPrompt(p0)
            emvHandler!!.onSetPromptResponse(true)
        }

        override fun onConfirmCardNo(p0: CardInfoEntity?) {
            super.onConfirmCardNo(p0)

            if (p0 != null) {
                Log.e("dd--", "onConfirmCardNo " + p0.tk2)
                Log.e("dd--", "onConfirmCardNo " + p0.cardNo)
                mCardNo = p0.cardNo
            }

            emvHandler!!.onSetConfirmCardNoResponse(true)
        }

        override fun onCardHolderInputPin(p0: Boolean, p1: Int) {
            super.onCardHolderInputPin(p0, p1)
            Log.e("dd--", "isOnlinePin: $p0")
            Log.e("dd--", "leftTimes $p1")

            mPinType = p0
            pinInput()
        }

        override fun onOnlineProc() {
            super.onOnlineProc()

            Log.d("dd--", "getEmvContactlessMode:" + emvHandler!!.emvContactlessMode)
            Log.d("dd--", "getcardinfo:" + Gson().toJson(emvHandler!!.emvCardDataInfo))
            Log.d("dd--", "getEmvCvmResult:" + emvHandler!!.emvCvmResult)
            Log.d("dd--", "getSignNeed--" + emvHandler!!.signNeed)

            val emvOnlineResult = EmvOnlineResultEntity()
            emvOnlineResult.authCode = "123450"
            emvOnlineResult.rejCode = "00"
            //fill with the host response 55 field EMV data to do second auth, the format should be TLV format.
            // for example: 910870741219600860008a023030  91 = tag, 08 = len, 7074121960086000 = value;
            // 8a = tag, 02 = len, 3030 = value
            //fill with the host response 55 field EMV data to do second auth, the format should be TLV format.
            // for example: 910870741219600860008a023030  91 = tag, 08 = len, 7074121960086000 = value;
            // 8a = tag, 02 = len, 3030 = value
            emvOnlineResult.recvField55 = null
            emvHandler!!.onSetOnlineProcResponse(SdkResult.Success, emvOnlineResult)
        }

        override fun onRemoveCard() {
            super.onRemoveCard()
            emvHandler!!.onSetRemoveCardResponse()
        }

        override fun onFinish(p0: Int, p1: EmvProcessResultEntity?) {
            super.onFinish(p0, p1)

            Log.d("dd--","Result : $p0")
            emvHandler!!.emvProcessCancel()
        }

    }

    private fun configPaypassParameter(aid: ByteArray) {
        //kernel configuration, enable RRP and cdcvm
        emvHandler!!.setTlv(
            byteArrayOf(0xDF.toByte(), 0x81.toByte(), 0x1B.toByte()),
            byteArrayOf(0x30.toByte())
        )

        //EMV MODE :amount >contactless cvm limit, set 60 = online pin and signature
        emvHandler!!.setTlv(
            byteArrayOf(0xDF.toByte(), 0x81.toByte(), 0x18.toByte()),
            byteArrayOf(0x60.toByte())
        )
        //EMV mode :amount < contactless cvm limit, set 08 = no cvm
        emvHandler!!.setTlv(
            byteArrayOf(0xDF.toByte(), 0x81.toByte(), 0x19.toByte()),
            byteArrayOf(0x08.toByte())
        )
        if (ByteUtils.byteArray2HexString(aid).uppercase().contains("A0000000043060")) {
            Log.e("dd--", "======maestro===== ")
            //maestro only support online pin
            emvHandler!!.setTlv(
                byteArrayOf(0x9F.toByte(), 0x33.toByte()), byteArrayOf(
                    0xE0.toByte(),
                    0x40.toByte(), 0xC8.toByte()
                )
            )
            emvHandler!!.setTlv(
                byteArrayOf(0xDF.toByte(), 0x81.toByte(), 0x18.toByte()), byteArrayOf(
                    0x40.toByte()
                )
            )
            emvHandler!!.setTlv(
                byteArrayOf(0xDF.toByte(), 0x81.toByte(), 0x19.toByte()), byteArrayOf(
                    0x08.toByte()
                )
            )
        }


    }

    private fun leftPad(amountStr: String, size: Int, padChar: Char): String {
        Log.e("dd--", "Enter leftPad()")
        Log.e(
            "dd--", "Initial Amount Str[$amountStr] : final string size[$size] : padChar[$padChar]"
        )
        val padded = StringBuilder(amountStr ?: "")
        while (padded.length < size) {
            padded.insert(0, padChar)
        }
        Log.e("dd--", "Final padded string: $padded")
        return padded.toString()
    }

    private fun pinInput() {

        sdkPinPad!!.setPinKeyboardMode(PinKeyboardModeEnum.FIXED)
        sdkPinPad!!.setAlgorithmMode(AlgorithmModeEnum.DES)

        if (mPinType!!) {

            if (mCardNo.isEmpty()) {
                mCardNo = emvHandler!!.emvCardDataInfo.cardNo
            }

            val pinL = intArrayOf(0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c)
            sdkPinPad!!.inputOnlinePin(
                pinL,
                60,
                mCardNo.toByteArray(),
                10,
                PinAlgorithmModeEnum.ISO9564FMT1,
                mPinPadCallback
            )
        }else{
            val pinL = intArrayOf(0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c)
            sdkPinPad!!.inputOfflinePin(pinL,60,mPinPadCallback)
        }

    }

    private val mPinPadCallback = object : PinPadCallback() {
        override fun onInputResult(p0: Int, p1: ByteArray?) {
            super.onInputResult(p0, p1)

            Log.e(
                "dd--",
                "Enter onInputResult() : retCode$p0"
            )
            Log.e(
                "dd--",
                "Encrypted Pin Block (EPB) = " + ByteUtils.byteArray2HexStringWithSpace(p1)
            )

            if (p0 == SdkResult.Success || p0 == SdkResult.PinPad_No_Pin_Input || p0 == SdkResult.PinPad_Input_Cancel) {
                if (p1 != null) {
                    val temp = ByteArray(8)
                    System.arraycopy(p1, 0, temp, 0, 8)
                }
                emvHandler!!.onSetPinInputResponse(
                    p0 != SdkResult.PinPad_Input_Cancel,
                    p0 == SdkResult.PinPad_No_Pin_Input
                )
            } else {
                Log.e("dd--", "pin enter fail")
                emvHandler!!.onSetPinInputResponse(false, false)
            }


        }

        override fun onSendKey(p0: Byte) {
            super.onSendKey(p0)
        }

    }


}



