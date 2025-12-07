package com.example.medace

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.medace.api.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var uploadButton: Button
    private lateinit var fileNameText: TextView
    private lateinit var ocrResult: TextView
    private lateinit var analysisResult: TextView
    private lateinit var chatInput: EditText
    private lateinit var sendChatButton: Button
    private lateinit var chatRecycler: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var menuToggle: ImageButton
    private lateinit var sidebar: LinearLayout
    private lateinit var mainCard: CardView
    private lateinit var rootContainer: ConstraintLayout

    private var isSidebarVisible = true
    private var extractedReportText: String = ""

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()

        uploadButton.setOnClickListener { pickImageFromGallery() }

        sendChatButton.setOnClickListener {
            val userMessage = chatInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                chatAdapter.addMessage(ChatMessage(userMessage, "user"))
                chatInput.text.clear()
                callHuggingFace(userMessage, isSummary = false)
            }
        }

        menuToggle.setOnClickListener { toggleSidebar() }
    }

    // ---------------------- UI SETUP ----------------------
    private fun initializeUI() {
        uploadButton = findViewById(R.id.uploadButton)
        fileNameText = findViewById(R.id.fileNameText)
        ocrResult = findViewById(R.id.ocrResult)
        analysisResult = findViewById(R.id.analysisResult)
        chatInput = findViewById(R.id.chatInput)
        sendChatButton = findViewById(R.id.sendChatButton)
        chatRecycler = findViewById(R.id.chatRecycler)
        menuToggle = findViewById(R.id.menuToggle)
        sidebar = findViewById(R.id.sidebar)
        mainCard = findViewById(R.id.mainCard)
        rootContainer = findViewById(R.id.root_container)

        chatAdapter = ChatAdapter()
        chatRecycler.layoutManager = LinearLayoutManager(this)
        chatRecycler.adapter = chatAdapter
    }

    // ---------------------- SIDEBAR ----------------------
    private fun toggleSidebar() {
        TransitionManager.beginDelayedTransition(rootContainer)
        sidebar.visibility = if (isSidebarVisible) View.GONE else View.VISIBLE
        isSidebarVisible = !isSidebarVisible
    }

    // ---------------------- IMAGE PICKER ----------------------
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = result.data?.data
            if (uri != null) {
                fileNameText.text = "File: ${uri.lastPathSegment}"
                recognizeTextFromImage(uri)
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // ---------------------- OCR ----------------------
    private fun recognizeTextFromImage(imageUri: Uri) {
        try {
            val stream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(stream)
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractedReportText = visionText.text
                    ocrResult.text = extractedReportText

                    if (extractedReportText.isNotEmpty()) {
                        chatAdapter.addMessage(ChatMessage("📄 Report extracted successfully.", "bot"))
                        getReportSummary()
                    } else {
                        chatAdapter.addMessage(ChatMessage("No readable text found.", "bot"))
                    }
                }
                .addOnFailureListener { e ->
                    chatAdapter.addMessage(ChatMessage("OCR Error: ${e.message}", "bot"))
                }

        } catch (e: Exception) {
            chatAdapter.addMessage(ChatMessage("Failed to process image: ${e.message}", "bot"))
        }
    }

    // ---------------------- SUMMARY USING HUGGINGFACE ----------------------
    private fun getReportSummary() {
        callHuggingFace("", isSummary = true)
    }

    // ---------------------------------------------------------
    // ---------------------- HUGGINGFACE CALL -----------------
    // ---------------------------------------------------------
    private fun callHuggingFace(userText: String, isSummary: Boolean) {

        val finalPrompt = if (isSummary) {
            "Summarize this medical report clearly:\n$extractedReportText"
        } else {
            userText
        }

        val placeholder = if (isSummary)
            "⏳ Summarizing report..."
        else
            "⏳ Thinking…"

        if (isSummary) analysisResult.text = placeholder
        else chatAdapter.addMessage(ChatMessage(placeholder, "bot"))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = HuggingFaceRequest(
                    messages = listOf(
                        Message("system", "You are a helpful medical assistant."),
                        Message("user", finalPrompt)
                    )
                )

                val response = RetrofitClientHuggingFace.instance.generateChat(request)
                val aiReply = response.choices.firstOrNull()?.message?.content ?: "No reply."
                "No response from HuggingFace."

                withContext(Dispatchers.Main) {
                    if (isSummary) {
                        analysisResult.text = aiReply
                    } else {
                        chatAdapter.updateLastMessage(aiReply)
                        chatRecycler.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val err = "HF Error: ${e.message}"
                    if (isSummary) analysisResult.text = err
                    else chatAdapter.updateLastMessage(err)
                }
            }
        }
    }

    // ---------------------------------------------------------
    // ---------------------- GROQ (KEPT BUT NOT USED) ---------
    // ---------------------------------------------------------
    private fun callGroqChatbot(userText: String, isSummary: Boolean = false) {
        // You said: keep Groq code, but don't use it
        // So we keep this function untouched.
        val placeholderMsg = "⏳ Analyzing..."
        if (isSummary) analysisResult.text = placeholderMsg
        else chatAdapter.addMessage(ChatMessage(placeholderMsg, "bot"))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = GroqRequest(
                    model = "Qwen/Qwen3-4B-Instruct-2507",
                    messages = listOf(
                        Message(
                            role = "system",
                            content = "You are MedAce AI. Use this medical report for context:\n$extractedReportText"
                        ),
                        Message(
                            role = "user",
                            content = userText
                        )
                    )
                )

                val response = RetrofitClient.instance.getChatResponse(request)
                val aiReply = response.choices.firstOrNull()?.message?.content ?: "No response."

                withContext(Dispatchers.Main) {
                    if (isSummary) analysisResult.text = aiReply
                    else {
                        chatAdapter.updateLastMessage(aiReply)
                        chatRecycler.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "Error: ${e.message}"
                    if (isSummary) analysisResult.text = errorMsg
                    else chatAdapter.updateLastMessage(errorMsg)
                }
            }
        }
    }
}
