package com.zoo.it.sreader

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import android.support.test.rule.ActivityTestRule
import com.google.android.gms.drive.DriveContents
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.MetadataBuffer
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileReader
import java.io.Reader


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ExampleInstrumentedTest {
    @get:Rule
    public val mActivityRule = ActivityTestRule<ActivityAll>(ActivityAll::class.java)


    @Test
    fun checkFilesFunctions() {
        signIn()
        var isAppFolder = false

        //createFile
        val createFileTask = createFile("file", "text/cmd", "Hello World".toByteArray())
                .addOnSuccessListener { create ->
                    //check File's creation
                    val task = existFile("file")

                    task.addOnSuccessListener { data ->
                        data.forEach {
                            if (it.isInAppFolder)
                                isAppFolder = true
                        }

                    }
                }

        Tasks.await(createFileTask)

        Tasks.whenAll(createFileTask).continueWithTask {
            downloadFile("file")
        }.addOnSuccessListener { downloadSuccess ->
            val byte = ByteArray(downloadSuccess.inputStream.available())
            downloadSuccess.inputStream.read(byte)
            assertEquals(String(byte), "Hello World")
            println("dgfdfg${String(byte)}")
        }
        println("appFolde = $isAppFolder")

        //deleteFile
        val list = deleteFile("file")
        isAppFolder = false
        Tasks.whenAll(list).continueWith {
            existFile("file").addOnSuccessListener { data ->
                data.forEach {
                    if (it.isInAppFolder)
                        isAppFolder = true
                }
            }.addOnFailureListener {
                throw Exception("test failed delete")
            }
        }

    }


    private fun signIn() {
        val method = mActivityRule.activity::class.java.superclass.getDeclaredMethod("signIn")
        method.isAccessible = true
        method.invoke(mActivityRule.activity)
    }

    private fun createFile(title: String, mimeType: String, content: ByteArray): Task<DriveFile> {
        val method = mActivityRule.activity::class.java.superclass.getDeclaredMethod("createFile",
                String::class.java, String::class.java, ByteArray::class.java)
        method.isAccessible = true
        return method.invoke(mActivityRule.activity, title, mimeType, content) as Task<DriveFile>
    }

    private fun existFile(title: String): Task<MetadataBuffer> {
        val method = mActivityRule.activity::class.java.superclass.getDeclaredMethod("existFile", String::class.java)
        method.isAccessible = true
        return method.invoke(mActivityRule.activity, title) as Task<MetadataBuffer>
    }

    private fun deleteFile(title: String): ArrayList<Task<Void>> {
        val method = mActivityRule.activity::class.java.superclass.getDeclaredMethod("deleteFileDrive", String::class.java)
        method.isAccessible = true
        return method.invoke(mActivityRule.activity, title) as ArrayList<Task<Void>>
    }

    private fun downloadFile(title: String): Task<DriveContents> {
        val method = mActivityRule.activity::class.java.superclass.getDeclaredMethod("downloadFile", String::class.java)
        method.isAccessible = true
        return method.invoke(mActivityRule.activity, title) as Task<DriveContents>
    }
}
