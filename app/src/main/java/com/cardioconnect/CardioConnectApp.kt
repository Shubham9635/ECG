package com.cardioconnect

import android.app.Application
import com.cardioconnect.data.ble.BleManager
import com.cardioconnect.data.database.EcgDatabaseHelper
import com.cardioconnect.data.export.CsvReportExporter
import com.cardioconnect.data.export.PdfReportExporter
import com.cardioconnect.data.repository.DeviceRepositoryImpl
import com.cardioconnect.data.repository.EcgRepositoryImpl
import com.cardioconnect.data.repository.SettingsRepositoryImpl
import com.cardioconnect.data.simulator.EcgWaveformSimulator
import com.cardioconnect.domain.repository.DeviceRepository
import com.cardioconnect.domain.repository.EcgRepository
import com.cardioconnect.domain.repository.SettingsRepository

class CardioConnectApp : Application() {

    lateinit var dbHelper: EcgDatabaseHelper
        private set
    lateinit var bleManager: BleManager
        private set
    lateinit var simulator: EcgWaveformSimulator
        private set

    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var deviceRepository: DeviceRepository
        private set
    lateinit var ecgRepository: EcgRepository
        private set
    lateinit var patientRepository: com.cardioconnect.domain.repository.PatientRepository
        private set

    lateinit var pdfExporter: PdfReportExporter
        private set
    lateinit var csvExporter: CsvReportExporter
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        dbHelper = EcgDatabaseHelper(this)
        bleManager = BleManager(this)
        simulator = EcgWaveformSimulator()

        settingsRepository = SettingsRepositoryImpl(this)
        deviceRepository = DeviceRepositoryImpl(bleManager)
        ecgRepository = EcgRepositoryImpl(bleManager, simulator, dbHelper, settingsRepository)
        patientRepository = com.cardioconnect.data.repository.PatientRepositoryImpl(this)

        pdfExporter = PdfReportExporter(this)
        csvExporter = CsvReportExporter(this)
    }

    companion object {
        lateinit var instance: CardioConnectApp
            private set
    }
}
