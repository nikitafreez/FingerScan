package com.example.fingerscan

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private var cancellationSignal: CancellationSignal? = null

    private val authenticatorCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                notifyUser("Ошибка при авторизации $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                notifyUser("Вы успешно авторизировались!")
                startActivity(Intent(this@MainActivity, SlaveActivity::class.java))
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBiometricSupport()
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        btnAuth.setOnClickListener {
            val biometricPrompt =
                BiometricPrompt.Builder(this).setTitle("Вход").setSubtitle("Авторизация в систему")
                    .setDescription("Необходимо для входа в приложение").setNegativeButton(
                        "Отмена",
                        this.mainExecutor,
                        DialogInterface.OnClickListener { dialog, which ->
                            notifyUser("Авторизация отменена...")
                        }).build()

            biometricPrompt.authenticate(
                getCancellationSignal(),
                mainExecutor,
                authenticatorCallback
            )
        }
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Пользователь отменил сканирвоание отпечатка пальца")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isKeyguardSecure) {
            notifyUser("Аутенфикация по отпечатку пальца не была включена в настройках")
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notifyUser("Вы не дали разрешение на работу с отпечатком пальца")
        }

        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    private fun notifyUser(messege: String) {
        Toast.makeText(this, messege, Toast.LENGTH_LONG).show()
    }
}