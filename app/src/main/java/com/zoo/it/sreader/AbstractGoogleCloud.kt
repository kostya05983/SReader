package com.zoo.it.sreader

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField

abstract class AbstractGoogleCloud : AppCompatActivity() {

    val REQUEST_CODE_SIGN_IN = 0

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mDriveClient: DriveClient
    private lateinit var mDriveResourceClient: DriveResourceClient

    protected fun signIn() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Drive.SCOPE_FILE)
        requiredScopes.add(Drive.SCOPE_APPFOLDER)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signInAccount != null && signInAccount.grantedScopes.containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount)
        } else {
            val googleSignInClient = buildGoogleSignInClient()
            startActivityForResult(googleSignInClient.signInIntent, REQUEST_CODE_SIGN_IN)
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

    //todo
    protected fun existFile() {

    }


    protected fun createFile(title: String, mimeType: String, content: ByteArray) {
        mDriveResourceClient.appFolder.continueWithTask {
            val parent = it.result
            val changeSet = MetadataChangeSet.Builder()
                    .setTitle(title)
                    .setMimeType(mimeType)
                    .setStarred(true)
                    .build()
            val contents = mDriveResourceClient.createContents().result
            contents.outputStream.write(content)
            mDriveResourceClient.createFile(parent, changeSet, contents)
        }.addOnSuccessListener {

        }.addOnFailureListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                if (resultCode != Activity.RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
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