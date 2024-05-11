package furhatos.app.isiser.setting

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.FileInputStream

fun getSheetsService(): Sheets {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory = JacksonFactory.getDefaultInstance()
    val credentialsFilePath = "/path/to/your/service-account.json" // Adjust this path

    val credential = GoogleCredential.fromStream(FileInputStream(credentialsFilePath))
        .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

    return Sheets.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName("Your Application Name")
        .build()
}

fun loadSheetData(spreadsheetId: String, range: String): List<List<Any>> {
    val service = getSheetsService()
    val response = service.spreadsheets().values()
        .get(spreadsheetId, range)
        .execute()

    return response.getValues()
}

