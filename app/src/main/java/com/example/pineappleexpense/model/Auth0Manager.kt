package com.example.pineappleexpense.model
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.result.Credentials
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.jwt.Claim
import com.auth0.android.jwt.JWT
import com.auth0.android.result.UserProfile

class Auth0Manager (private val context: Context, application: Application) {

    // Keys for shared preferences
    private val accessToken = "access_token"
    private val idToken = "id_token"
    private val refreshToken = "refresh_token"
    private val name = "name"
    private val email = "email"
    private val id = "id"
    private val expiresAt = "expires_at"
    private val companyName = "company_name"
    private val roleType = "role_type"

    // Initialize auth0
    private val auth0 =
        Auth0(
            "NOYs1STlitUk7Ve049kR93wbOoXAafsq",
            "pineappleauth.ximixu.dev"
        )

    // Initialize credentials manager
    private var credentialsManager = SecureCredentialsManager(
        application.applicationContext,
        AuthenticationAPIClient(auth0),
        SharedPreferencesStorage(application.applicationContext)
    )

    fun storeCredentials(credentials: Credentials) {
        credentialsManager.saveCredentials(credentials)
    }

    fun storeTokens(credentials: Credentials) {
        saveTokens(credentials)
    }

    fun storeUserInfo(profile: UserProfile) {
        saveUserInfo(profile)
    }

    fun getCredentialsManager(): SecureCredentialsManager {
        return credentialsManager
    }

    /**
     * Check if the user is running a new session of the app.
     * New sessions apply to users who were not already logged in.
     */
    fun getSessionStatus(): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("new_session", true)
    }

    /**
     * Changes the session status.
     */
    fun changeSessionStatus(status: Boolean) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("new_session", status)
            .apply()
    }

    /**
     * Check if credentials in the credentials manager are still valid.
     */
    fun validate(): Boolean {
        return credentialsManager.hasValidCredentials()
    }

    /**
     * Returns the stored access token as a string.
     */
    fun getAccess(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(accessToken, toString())
    }

    /**
     * Returns the stored id token as a string.
     */
    fun getIDToken(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(idToken, toString())
    }

    /**
     * Returns the expiry date for the access token as a string.
     */
    fun getExpireTime(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(expiresAt, toString())
    }

    /**
     * Clears the data stored in the shared preferences and credentials manager.
     */
    fun clearData() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .clear().apply()
        credentialsManager.clearCredentials()
    }

    /**
     * Returns the name of the user.
     */
    fun getName(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(name, toString())
    }

    /**
     * Returns the email of the user.
     */
    fun getEmail(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(email, toString())
    }

    /**
     * Returns the unique ID of the user; not to be confused with the id token.
     */
    fun getId(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(id, toString())
    }

    fun getCompany(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(companyName, toString())
    }

    fun getRole(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferences.getString(roleType, toString())
    }

    /**
     * Private function. Save the relevant auth0 tokens to shared preferences.
     */
    private fun saveTokens(credentials: Credentials) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(accessToken, credentials.accessToken)
            .putString(idToken, credentials.idToken)
            .putString(refreshToken, credentials.refreshToken)
            .putString(expiresAt, credentials.expiresAt.toString())
            .apply()
        setRole(credentials.idToken)
    }

    /**
     * Private function. Save user information to shared preferences.
     */
    private fun saveUserInfo(profile: UserProfile) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(name, profile.name)
            .putString(email, profile.email)
            .putString(id, profile.getId())
            .putString(companyName, profile.nickname)
            .apply()
    }

    private fun setRole(idToken: String) {
        var jwt = JWT(idToken)
        var claim: String? = jwt.getClaim("roleType").asString()
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(roleType, claim)
            .apply()
    }
}