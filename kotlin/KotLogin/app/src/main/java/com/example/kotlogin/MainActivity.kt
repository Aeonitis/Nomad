package com.example.kotlogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var signInClient: GoogleSignInClient
    lateinit var signInOptions: GoogleSignInOptions

    val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        initializeUI()
        setupGoogleLogin()

    }

    override fun onStart() {
        super.onStart()

        //Ensure no multiple instances Auth object running at the same time
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(LoggedInActivity.getLaunchIntent(this))
            finish()
        }
    }

    /**
     * Request the user data
     */
    private fun setupGoogleLogin() {
        signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        signInClient = GoogleSignIn.getClient(this, signInOptions)
    }

    /**
     * Initialize our UI and setup onClick Listeners on the button in our main Activity
     */
    private fun initializeUI() {
        google_button.setOnClickListener {
            login()
        }
    }

    /**
     * Start the login process, which will be called when the user clicks on the log-in button.
     * The user will then be asked to select an account for authentication.
     * The signInIntent is used to handle signing in and also the startActivityForResult() is used.
     */
    private fun login() {
        val loginIntent: Intent = signInClient.signInIntent
        startActivityForResult(loginIntent, RC_SIGN_IN)

    }

    /**
     * After the login method is invoked the onActivityResult() is called.
     * The selected authentication account is retrieved and sent to Firebase to
     * complete the authentication process.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    googleFirebaseAuth(account)
                    printAccountDetails(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Gets ID token from the GoogleSignInAccount object, and exchanges it for a Firebase credential
     * Also authenticates with Firebase using the obtained Firebase credential.
     */
    private fun googleFirebaseAuth(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                startActivity(LoggedInActivity.getLaunchIntent(this))
            } else {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    /**
     * Print all user data :'(
     */
    private fun printAccountDetails(account: GoogleSignInAccount) {
        println(account.id)
        println(account.givenName)
        println(account.account)
        println(account.email)
        println(account.idToken)
        println(account.photoUrl)
        println(account.grantedScopes)
        println(account.isExpired)
        println(account.requestedScopes)
        println(account.serverAuthCode)
    }
}
