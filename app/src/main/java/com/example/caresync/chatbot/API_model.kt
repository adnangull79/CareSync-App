// File: ChatViewModel.kt
package com.example.caresync.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val messages: StateFlow<List<Pair<String, Boolean>>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val apiKey = Constants.API_KEY

    // ✅ Marks if conversation has started with a health question
    private var hasStartedConversation = false

    // ✅ Keyword list (English + Roman Urdu + local terms)
    private val healthKeywords = listOf(
        // Core Medical Context
        "health", "medical", "doctor", "nurse", "clinic", "hospital", "checkup", "treatment",
        "medication", "medicine", "drug", "dose", "prescription", "therapy", "diagnosis",
        "consultation", "appointment", "surgeon",

        // Symptoms / Complaints
        "pain", "ache", "sore", "swelling", "rash", "itching", "burning",
        "numbness", "weakness", "fatigue", "dizzy", "nausea", "vomiting",
        "diarrhea", "constipation", "fever", "chills", "cough", "flu", "infection",
        "migraine", "headache", "pressure", "cramps", "gas", "acidity", "bloating",

        // Vital signs & Chronic conditions
        "blood pressure", "bp", "heart rate", "pulse", "oxygen", "temperature",
        "diabetes", "sugar level", "cholesterol", "obesity",

        // Body Parts
        "heart", "lungs", "kidney", "liver", "stomach", "intestine", "thyroid",
        "skin", "brain", "nerve", "spine", "chest", "throat", "eye", "ear", "nose",

        // Common Diseases
        "asthma", "allergy", "arthritis", "ulcer", "pneumonia", "hepatitis",
        "stroke", "thyroid disorder", "pcos", "gastritis", "reflux", "anemia",
        "malaria", "dengue", "typhoid", "cancer",

        // Diagnostics & Tests
        "x-ray", "mri", "ct scan", "ultrasound", "blood test", "cbc",
        "ecg", "echo", "biopsy", "lipid profile",

        // Medicines
        "antibiotic", "painkiller", "paracetamol", "ibuprofen", "aspirin",
        "syrup", "tablet",

        // Nutrition & Fitness
        "diet", "nutrition", "protein", "vitamin", "exercise", "fitness", "sleep",

        // Women Health
        "pregnancy", "period", "menstrual", "breastfeeding",

        // Children Health
        "pediatric", "teething", "child fever",

        // Mental Health
        "stress", "anxiety", "depression", "insomnia",

        // Emergencies
        "bleeding", "fracture", "burn", "injury", "choking", "chest pain",

        // ✅ Roman Urdu Local Terms
        "bimari", "bemar", "tabiyat", "kamzori", "susti", "ghabrahat", "chakkar",
        "sar dard", "pet dard", "pait dard", "gas", "ultee", "zukaam", "khansi",
        "saans phoolna", "seene ka dard", "dast", "qabz", "pishaab", "jigar", "gurda",
        "mahvari", "doodh band", "bachay ka bukhar", "panadol", "brufen", "flagyl",
        "augmentin", "zinco", "inhaler", "jal jana", "haddi toot jana", "accident",
        "bimari", "bemar", "tabiyat", "halat", "halat kharab", "halka bukhar", "tez bukhar",
        "bukhar utarna", "kapkapi", "paseena", "ghabrahat", "bechaini", "susti", "kamzori",
        "behosh", "chakkar", "sar ghoomna", "dimagh", "sar", "sar dard", "severe sar dard",

        "pet", "pet dard", "pait dard", "acid", "tezab", "gas", "hajma", "pet phoolna", "qabz",
        "dast", "ultee", "ghabrahat aur ultee", "jalan pait mein", "bhook kam lagna",

        "saans", "saans phoolna", "saans mein takleef", "seene ka dard", "sine me dard",
        "sina dard", "khansi", "balgham", "dama", "ghutan", "nafas ki tangi","Tips",

        "gala", "gala dard", "gala khushk", "tonsil", "zukaam", "naak band", "nath band","First Aid",

        "pishaab", "pishaab mein jalan", "bar bar pishaab", "kam pishaab", "gurda", "kidney pathri",

        "period", "mahvari", "dard-e-mahvari", "bacha daani", "hormonal masail",
        "breast dard", "doodh band hona", "feeding masla",

        "bachay ka bukhar", "tez bukhar bachay ko", "khaansi bachay ko", "teething",
        "pait dard bachay ko", "doodh nahi pee raha", "roney wala bacha",

        "panadol", "calpol", "brufen", "flexon", "ponstan", "gravinate", "ORS", "flagyl",
        "augmentin", "zinco", "ventolin", "inhaler",

        "chot", "zaher", "poison", "jal jana", "fracture", "haddi toot jana", "be-hoshi",
        "tezi se saans", "khoon behna", "accident", "gir jana", "sine me jalan"

    )

    // ✅ Health detection only for first message
    private fun isHealthRelated(message: String): Boolean {
        val m = message.lowercase()
        return healthKeywords.any { keyword -> m.contains(keyword) }
    }

    // ✅ Build conversation context (last 3 exchanges)
    private fun buildConversationContext(newMessage: String): String {
        val lastMessages = _messages.value.takeLast(6)
        val historyText = lastMessages.joinToString("\n") { (text, isUser) ->
            if (isUser) "User: $text" else "Assistant: $text"
        }
        return "$historyText\nUser: $newMessage"
    }

    fun sendMessageToGemini(userMessage: String) {

        _messages.value = _messages.value + (userMessage to true)

        // ✅ First message must be health-related
        if (!hasStartedConversation && !isHealthRelated(userMessage)) {
            _messages.value = _messages.value + (
                    "I'm a health assistant and can only answer health-related questions.\n\n" +
                            "Please ask about symptoms, medicines, treatment, diet, or wellness.\n\n" +
                            "Also check spelling — if a health term is spelled incorrectly, I may not detect it.\n\n" +
                            "How can I help you with your health today?" to false
                    )
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val contextMessage = buildConversationContext(userMessage)

                val systemPrompt = """
                    You are a professional health assistant for the CareSync medical app.
                    Always provide accurate, safe, patient-friendly health guidance.
                    Include this disclaimer: "⚠️ I can guide you, but your health 
                    deserves a proper checkup. 
                    Please consult a doctor for accurate diagnosis and care."
                    
                    Conversation so far:
                    $contextMessage
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content("user", listOf(Part(systemPrompt))))
                )

                val response = RetrofitInstance.api.getChatReply(apiKey, request)

                val reply = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?: "Sorry, I couldn't process that. Please try again."

                _isLoading.value = false

                // ✅ Mark conversation as started
                hasStartedConversation = true

                _messages.value = _messages.value + (reply to false)

            } catch (e: Exception) {
                _isLoading.value = false
                _messages.value = _messages.value + (
                        "Sorry, I'm having trouble connecting. Try again later." to false
                        )
            }
        }
    }
}
