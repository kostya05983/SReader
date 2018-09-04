package com.zoo.it.sreader

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks


/**
 * @author Konstantin Volivach ;)
 */
abstract class AbstractGoogleCloud : AbstractNavigation(), NavigationView.OnNavigationItemSelectedListener {

    private val requestCodeSignIn = 0
    private val tagCloud = "GoogleCloud"

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mDriveClient: DriveClient
    private lateinit var mDriveResourceClient: DriveResourceClient


    /**
     * fun makes a sign in
     * if user was signed in fun does't call activity for result
     * alternatively it creates a activity with request to authorize
     */
    protected fun signIn() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Drive.SCOPE_FILE)
        requiredScopes.add(Drive.SCOPE_APPFOLDER)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signInAccount != null && signInAccount.grantedScopes.containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount)
        } else {
            val googleSignInClient = buildGoogleSignInClient()
            startActivityForResult(googleSignInClient.signInIntent, requestCodeSignIn)
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        mDriveClient = Drive.getDriveClient(applicationContext, signInAccount)
        mDriveResourceClient = Drive.getDriveResourceClient(applicationContext, signInAccount)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .build()
        return GoogleSignIn.getClient(this, signInOptions)
    }

    /**
     * fun check file's existence
     * it gets the request's result
     * if results are null it return false
     * if results aren't in app's folder it returns false
     * else it returns true
     * @param title - the file's name
     */
    protected fun existFile(title: String): Task<MetadataBuffer> {
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, title))
                .build()
        return mDriveResourceClient.query(query)
    }

    /**
     * fun to create file with mimeType on google Drive
     * @param title - the file's name to create
     * @param mimeType - the file's type
     * @param content - the file's data in bytes
     */
    protected fun createFile(title: String, mimeType: String, content: ByteArray): Task<DriveFile> {
        val appFolderTask = mDriveResourceClient.appFolder
        val createContentsTask = mDriveResourceClient.createContents()
        return Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask {
                    val parent = appFolderTask.result
                    val contents = createContentsTask.result
                    contents.outputStream.write(content)
                    val changeSet = MetadataChangeSet.Builder()
                            .setTitle(title)
                            .setMimeType(mimeType)
                            .setStarred(true)
                            .build()
                    mDriveResourceClient.createFile(parent, changeSet, contents)
                }.addOnSuccessListener {
                    Toast.makeText(this, "Успешно добавлено", Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Ошибка, информация отправлена администратору", Toast.LENGTH_LONG).show()
                    Log.e(tagCloud, "fail to load", it)
                }
    }

    /**
     * fun to delete file from google drive in appFolder
     * firstly it execute query and return metaBuffer
     * secondly it goes forEach metadata and delete it
     * @param title - the name of file to delete
     */
    protected fun deleteFileDrive(title: String): ArrayList<Task<Void>> {
        val list = ArrayList<Task<Void>>()
        val task = mDriveResourceClient.appFolder.continueWithTask {
            val query = Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, title))
                    .build()
            mDriveResourceClient.query(query)
        }.addOnSuccessListener { metaBuffer ->
            metaBuffer.forEach { metaData ->
                val driveResource = metaData.driveId.asDriveResource()
                list.add(mDriveResourceClient.delete(driveResource).addOnSuccessListener {
                    Toast.makeText(this, "Книга с облака удалена успешно", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Произошла ошибка во время удаления, сообщение отправлено администратору", Toast.LENGTH_LONG).show()
                    Log.e(tagCloud, "error while deleting file from Drive, fileName=$title")
                })
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Файл не найден, сообщение отправлено администратору", Toast.LENGTH_LONG).show()
            Log.e(tagCloud, "Mistake for finding File $title")
        }
        Tasks.await(task)
        return list
    }

    /**
     * fun for downloading file
     * @param title - the file's name
     */
    protected fun downloadFile(title: String): Task<DriveContents> {
        return mDriveResourceClient.appFolder.continueWithTask {
            val query = Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, title))
                    .build()
            val task = mDriveResourceClient.query(query)
            val metadata = task.result
            mDriveResourceClient.openFile(metadata[0].driveId.asDriveFile(), DriveFile.MODE_READ_ONLY)
        }.addOnSuccessListener {
            Toast.makeText(this, "Книга $title успешно загружена", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            Log.e(tagCloud, "Downloading's mistake with $title")
            Toast.makeText(this, "Произошла ошибка при загрузке книги $title, сообщение отправлено администратору"
                    , Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestCodeSignIn -> {
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Авторизация успешна", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (getAccountTask.isSuccessful) {
                    initializeDriveClient(getAccountTask.result)
                } else {
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }



}