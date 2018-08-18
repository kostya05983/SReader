package com.zoo.it.sreader

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*

class ActivityAll : AppCompatActivity() {

    val REQUEST_CODE_SIGN_IN = 0
    val REQUEST_CODE_CAPTURE_IMAGE = 1
    val REQUEST_CODE_CREATOR = 2

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mDriveClient: DriveClient
    private lateinit var mDriveResourceClient: DriveResourceClient
    private lateinit var mBitmapToSave: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signIn()
        createFolder()
        setContentView(R.layout.activity_all)
    }

    private fun signIn() {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Drive.SCOPE_FILE)
        requiredScopes.add(Drive.SCOPE_APPFOLDER)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signInAccount != null && signInAccount.grantedScopes.containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount)
        } else {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Drive.SCOPE_FILE)
                    .requestScopes(Drive.SCOPE_APPFOLDER)
                    .build()
            val googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
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
                .build()
        return GoogleSignIn.getClient(this, signInOptions)
    }

    /**
     * fun creates folder on google drive
     */
    private fun createFolder() {
        mDriveResourceClient.rootFolder.continueWithTask {
            val changeSet = MetadataChangeSet.Builder()
                    .setTitle("New Folder")
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setStarred(true)
                    .build()
            mDriveResourceClient.createFolder(it.result, changeSet)
        }.addOnSuccessListener(this) {
            //todo add what do on success
            finish()
        }
                .addOnFailureListener(this) {
                    //todo add what do on failure
                    finish()
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
