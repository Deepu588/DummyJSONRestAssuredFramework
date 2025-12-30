package edu.qts.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.google.gson.Gson;
import edu.qts.pojos.TestReportData;


public class ExtentReportManager {



	public static String getReportPath() {
		
		String reportName = edu.qts.utils.TimeStampAndNamesManager.getReportName();

		File reportsDir = new File("reports");
		if (!reportsDir.exists()) {
			reportsDir.mkdir();
		}

		return       System.getProperty("user.dir")+ File.separator+ "reports" + File.separator + reportName;
	}


	

//	 private static String getGeminiApiKey() {
//	        String apiKey = System.getenv("GEMINI_API_KEY");
//	        if (apiKey != null && !apiKey.isEmpty()) {
//	            return apiKey;
//	        }
//	        return "YOUR_GEMINI_API_KEY_HERE"; // Replace with your key
//	    }
//	    

	
	
	
	
	public static ExtentSparkReporter createSparkReporter(String path) {


		ExtentSparkReporter sparkReporter = new ExtentSparkReporter(path);
		sparkReporter.config().setDocumentTitle("API  Automation Test Report");
		sparkReporter.config().setReportName("DummyJSON Test Results");
		sparkReporter.config().setTheme(Theme.DARK);
		sparkReporter.config().setCss(getCss());
		
		return sparkReporter;
	}

	public static void injectChatbotWithGeminiAI(TestReportData data,String reportPath) {
        try {
           // String reportPath = "test-output/ExtentReport.html";
            String content = new String(Files.readAllBytes(Paths.get(reportPath)));
            
            String chatbotHTML = getChatbotHTML();
            String chatbotScript = getChatbotScriptWithGemini(data);
            
            content = content.replace("</body>", chatbotHTML+chatbotScript  + "</body>");
            
            Files.write(Paths.get(reportPath), content.getBytes());
            
            System.out.println("AI chatbot injected with Extent metadata support!");
        } catch (IOException e) {
            System.err.println("Error injecting chatbot: " + e.getMessage());
        }
    }
	
	
	
	
	public static  ExtentReports   setSystemInformation(ExtentReports report) {
		report.setSystemInfo("Website Name", "Dummy JSON [Products]");
		report.setSystemInfo("Tester Name", System.getProperty("user.name"));
		report.setSystemInfo("Reporting Manager", "Deepak Jose");
		report.setSystemInfo("Environment", "QA");
		report.setSystemInfo("OS Name", System.getProperty("os.name"));
		//report.setSystemInfo("OS Version", System.getProperty("os.version"));
		report.setSystemInfo("Java Version", System.getProperty("java.version"));
		//report.setSystemInfo("Maven Version","3.9.7");
	
		return report;
	}


	 private static String getChatbotHTML() {
		 String customrHTML="<style>"+
	            "#chatbot-container { position: fixed; bottom: 8px; right: 20px; z-index: 9999; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }"+
	            "#chatbot-toggle { width: 65px; height: 65px; border-radius: 50%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4); transition: all 0.3s ease; animation: pulse 2s infinite; }"+
	            "@keyframes pulse { 0%, 100% { box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4); } 50% { box-shadow: 0 6px 30px rgba(102, 126, 234, 0.6); } }"+
	            "#chatbot-toggle:hover { transform: scale(1.1); }"+
	            "#chatbot-icon { font-size: 32px; }"+
	            "#chatbot-window { display: none; width: 420px; height: 650px; background: white; border-radius: 16px; box-shadow: 0 12px 48px rgba(0,0,0,0.3); flex-direction: column; position: absolute; bottom: 45px; right: 0; }"+
	            ".chatbot-open #chatbot-window { display: flex; animation: slideUp 0.3s ease; }"+
	            "@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }"+
	            ".chatbot-open #chatbot-toggle { display: none; }"+
	            "#chatbot-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 18px 20px; display: flex; justify-content: space-between; align-items: center; border-radius: 16px 16px 0 0; }"+
	            "#chatbot-header h3 { margin: 0; font-size: 18px; font-weight: 600; }"+
	            ".ai-badge { background: rgba(255,255,255,0.2); padding: 4px 10px; border-radius: 12px; font-size: 11px; margin-left: 8px; display: inline-block; font-weight: 500; }"+
	            "#chatbot-close { background: rgba(255,255,255,0.2); border: none; color: white; font-size: 24px; cursor: pointer; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; transition: background 0.2s; }"+
	            "#chatbot-close:hover { background: rgba(255,255,255,0.3); }"+
	            "#chatbot-messages { flex: 1; overflow-y: auto; padding: 20px; background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%); }"+
	            ".message { margin-bottom: 14px; padding: 11px 15px; border-radius: 14px; max-width: 85%; word-wrap: break-word; animation: fadeIn 0.3s ease; font-size: 14px; line-height: 1.5; position: relative; }"+
	            "@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }"+
	            ".user-message { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; margin-left: auto; border-bottom-right-radius: 4px; }"+
	           ".bot-message { background: white; color: #2d3748; border: 1px solid #e2e8f0; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }"+
	            ".ai-response { background: linear-gradient(135deg, #f0f4ff 0%, #ffffff 100%); border: 1px solid #c7d2fe; }"+
	            ".thinking-indicator { display: flex; gap: 5px; padding: 12px 16px; align-items: center; font-size: 13px; color: #64748b; }"+
	            ".thinking-dot { width: 8px; height: 8px; border-radius: 50%; background: #667eea; animation: thinking 1.4s infinite; }"+
	            ".thinking-dot:nth-child(2) { animation-delay: 0.2s; }"+
	            ".thinking-dot:nth-child(3) { animation-delay: 0.4s; }"+
	            "@keyframes thinking { 0%, 60%, 100% { transform: translateY(0); opacity: 0.5; } 30% { transform: translateY(-8px); opacity: 1; } }"+
	            ".quick-questions { padding: 12px 15px; background: white; border-top: 1px solid #e2e8f0; display: flex; flex-wrap: wrap; gap: 6px; }"+
	            ".quick-btn { padding: 7px 13px; background: #f7fafc; border: 1px solid #e2e8f0; border-radius: 16px; font-size: 12px; cursor: pointer; transition: all 0.2s; color: #4a5568; font-weight: 500; }"+
	            ".quick-btn:hover { background: #667eea; color: white; border-color: #667eea; transform: translateY(-1px); }"+
	            "#chatbot-input-container { display: flex; padding: 14px; gap: 8px; border-top: 1px solid #e2e8f0; background: white; border-radius: 0 0 16px 16px; }"+
	            "#chatbot-input { flex: 1; padding: 11px 16px; border: 2px solid #e2e8f0; border-radius: 24px; font-size: 14px; transition: all 0.2s; }"+
	            "#chatbot-input:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1); }"+
	            "#send-btn { padding: 11px 22px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 24px; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3); }"+
	            "#send-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4); }"+
	            "#send-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }"+
	            "</style>"+
	            
	            "<div id=\"chatbot-container\">"+
	                "<div id=\"chatbot-toggle\" onclick=\"toggleChatbot()\">"+
	                    "<span id=\"chatbot-icon\">🤖</span>"+
	                "</div>"+
	                "<div id=\"chatbot-window\">"+
	                    "<div id=\"chatbot-header\">"+
	                        "<div><h3>Test Assistant <span class=\"ai-badge\">Dummy JSON App</span></h3></div>"+
	                        "<button id=\"chatbot-close\" onclick=\"toggleChatbot()\">×</button>"+
	                    "</div>"+
	                    "<div id=\"chatbot-messages\">"+
	                        "<div class=\"message bot-message\">👋 Hi! Ask me about your test results, authors, devices, or any metadata!</div>"+
	                    "</div>"+
	                    "<div class=\"quick-questions\">"+
	                        "<span class=\"quick-btn\" onclick=\"askQuestion('Show test summary')\">📊 Summary</span>"+
	                        "<span class=\"quick-btn\" onclick=\"askQuestion('List tests by author')\">👤 Authors</span>"+
	                        "<span class=\"quick-btn\" onclick=\"askQuestion('Show device coverage')\">📱 Devices</span>"+
	                        "<span class=\"quick-btn\" onclick=\"askQuestion('Failed tests')\">❌ Failures</span>"+
	                    "</div>"+
	                    "<div id=\"chatbot-input-container\">"+
	                        "<input type=\"text\" id=\"chatbot-input\" placeholder=\"Ask about tests...\" />"+
	                        "<button id=\"send-btn\" onclick=\"sendMessage()\">Send</button>"+
	                    "</div>"+
	                "</div>"+
	            "</div>"
	            ;
		 return customrHTML;
	    }

	 private static String getUrlWithKey() {
	        //String urlWithKey = GEMINI_API_URL + "?key=" + apiKey.trim();

		String urlWithKey= EnvLoader.getAPIUrl().trim();
		return urlWithKey;
	 }
	 
	 private static String getChatbotScriptWithGemini(TestReportData data) {
		    Gson gson = new Gson();
		    String dataJson = gson.toJson(data);
		    String apiKey = getUrlWithKey();

		    String script = 
		        "<script>\n" +
		        "const testReportData = " + dataJson + ";\n" +
		        "const GEMINI_API_KEY = '" + apiKey + "';\n" +
		        "let isProcessing = false;\n" +
		        "\n" +
		        "function toggleChatbot() {\n" +
		        "    document.getElementById('chatbot-container').classList.toggle('chatbot-open');\n" +
		        "}\n" +
		        "\n" +
		        "function askQuestion(q) {\n" +
		        "    document.getElementById('chatbot-input').value = q;\n" +
		        "    sendMessage();\n" +
		        "}\n" +
		        "\n" +
		        "async function sendMessage() {\n" +
		        "    if (isProcessing) return;\n" +
		        "    const input = document.getElementById('chatbot-input');\n" +
		        "    const msg = input.value.trim();\n" +
		        "    if (!msg) return;\n" +
		        "    addMessage(msg, 'user');\n" +
		        "    input.value = '';\n" +
		        "    isProcessing = true;\n" +
		        "    document.getElementById('send-btn').disabled = true;\n" +
		        "    showThinking();\n" +
		        "    try {\n" +
		       // "        const localResponse = generateLocalResponse(msg);\n" +
		        //"        if (localResponse) {\n" +
		        //"            removeThinking();\n" +
		        //"            addMessage(localResponse, 'bot');\n" +
		        //"        } else {\n" +
		        "            const aiResponse = await askGeminiAI(msg);\n" +
		        "            removeThinking();\n" +
		        "            addMessage(aiResponse, 'ai');\n" +
		       // "        }\n" +
		        "    } catch (error) {\n" +
		        "        removeThinking();\n" +
		        "        addMessage('Sorry, error: ' + error.message, 'bot');\n" +
		        "    }\n" +
		        "    isProcessing = false;\n" +
		        "    document.getElementById('send-btn').disabled = false;\n" +
		        "}\n" +
		        "\n" +
//		        "async function askGeminiAI(question) {\n" +
//		        "    const context = buildContextForAI();\n" +
//		        "    const prompt = `You are a test automation expert. Here is the test data with metadata:\\n\\n${context}\\n\\nUser Question: ${question}\\n\\nProvide a helpful, concise answer based on the test data and metadata (authors, devices, categories, descriptions).`;\n" +
//		        "\n" +
//		        "    try {\n" +
//		        "        const response = await fetch( 'http://localhost:8088/api/chat', {\n" +
//		        "            method: 'POST',\n" +
//		        "            headers: { 'Content-Type': 'application/json' },\n" +
//		        "            body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] })\n" +
//		        "        });\n" +
//		        "        if (!response.ok) throw new Error('API request failed');\n" +
//		        "        const data = await response.json();\n" +
//		        "        return data.candidates[0].content.parts[0].text;\n" +
//		        "    } catch (error) {\n" +
//		        "        return 'Could not connect to AI service. Check API key.';\n" +
//		        "    }\n" +
//		        "}\n" +

"async function askGeminiAI(question) {\n" +
"    const context = buildContextForAI();\n" +
"    const prompt = `You are a test automation expert analyzing test results.\\n\\nTEST DATA:\\n${context}\\n\\nQUESTION: ${question}\\n\\nProvide a concise, helpful answer with specific details from the data.`;\n" +
"    try {\n" +
"        const response = await fetch('http://localhost:8088/api/chat', {\n" +
"            method: 'POST',\n" +
"            headers: { 'Content-Type': 'application/json' },\n" +
"            body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] })\n" + 
"        });\n" +
"        if (!response.ok) {\n" +
"            const errorText = await response.text();\n" +
"            throw new Error('Proxy error: ' + response.status + ' - ' + errorText);\n" +
"        }\n" +
"        const data = await response.json();\n" +
"        if (data.success === false) {\n" + 
"            throw new Error(data.error || 'Unknown error');\n" +
"        }\n" +

"        return data.response;\n" +  
"    } catch (error) {\n" +
"        console.error('AI Error:', error);\n" +
"        if (error.message.includes('Failed to fetch')) {\n" +
"            return '❌ Cannot connect to proxy server at localhost:8088.\\n\\nPlease ensure the proxy is running.';\n" +
"        }\n" +
"        return '❌ Error: ' + error.message;\n" +
"    }\n" +
"}\n" +
		        
		        
		        
		        "\n" +
		        "function buildContextForAI() {\n" +
//		        "    const data = testReportData;\n" +
//		        "    let context = `Suite: ${data.suiteName}\\nTotal: ${data.totalTests} | Passed: ${data.passed} | Failed: ${data.failed}\\n\\n`;\n" +
//		        "    let allAuthors = new Set();\n" +
//		        "    let allDevices = new Set();\n" +
//		        "    data.tests.forEach(t => {\n" +
//		        "        if (t.authors) t.authors.forEach(a => allAuthors.add(a));\n" +
//		        "        if (t.devices) t.devices.forEach(d => allDevices.add(d));\n" +
//		        "    });\n" +
//		        "    if (allAuthors.size > 0) context += `Authors: ${Array.from(allAuthors).join(', ')}\\n`;\n" +
//		        "    if (allDevices.size > 0) context += `Devices: ${Array.from(allDevices).join(', ')}\\n\\n`;\n" +
//		        "    if (data.failed > 0) {\n" +
//		        "        context += 'Failed Tests:\\n';\n" +
//		        "        data.tests.filter(t => t.status === 'FAILED').forEach((t, i) => {\n" +
//		        "            context += `${i+1}. ${t.testName}`;\n" +
//		        "            if (t.authors && t.authors.length > 0) context += ` (Author: ${t.authors.join(', ')})`;\n" +
//		        "            context += ` - ${t.errorMessage || 'No error'}\\n`;\n" +
//		        "        });\n" +
//		        "    }\n" +
		        "    return JSON.stringify(testReportData, null, 2);\r\n"+
		        
		        //"    return context;\n" +
		        "}\n" +
		        "\n" +
		        "function showThinking() {\n" +
		        "    const div = document.createElement('div');\n" +
		        "    div.id = 'thinking';\n" +
		        "    div.className = 'message bot-message thinking-indicator';\n" +
		        "    div.innerHTML = '<span style=\\\"margin-right: 8px;\\\">🤔 Thinking</span>' +\n" +
		        "                    '<div class=\\\"thinking-dot\\\"></div>' +\n" +
		        "                    '<div class=\\\"thinking-dot\\\"></div>' +\n" +
		        "                    '<div class=\\\"thinking-dot\\\"></div>';\n" +
		        "    document.getElementById('chatbot-messages').appendChild(div);\n" +
		        "    scrollToBottom();\n" +
		        "}\n" +
		        "\n" +
		        "function removeThinking() {\n" +
		        "    const t = document.getElementById('thinking');\n" +
		        "    if (t) t.remove();\n" +
		        "}\n" +
		        "\n" +
		        "function addMessage(text, type) {\n" +
		        "    const div = document.createElement('div');\n" +
		        "    const className = type === 'ai' ? 'message bot-message ai-response' : 'message ' + type + '-message';\n" +
		        "    div.className = className;\n" +
		        "    div.innerHTML = formatMessage(text);\n" +
		        "    document.getElementById('chatbot-messages').appendChild(div);\n" +
		        "    scrollToBottom();\n" +
		        "}\n" +
		        "\n" +
		        "function formatMessage(text) {\n" +
		        "    text = text.replace(/\\n/g, '<br>');\n" +
		        "    text = text.replace(/\\*\\*(.*?)\\*\\*/g, '<b>$1</b>');\n" +
		        "    return text;\n" +
		        "}\n" +
//		        "function formatMessage(text) {\n" +
//		        "    text = text.replace(/\\\\n/g, '<br>');\n" +   // double escaping \\n → \\n in JS, but valid in Java
//		        "    return text;\n" +
//		        "}\n" +
		        "\n" +
		        "function scrollToBottom() {\n" +
		        "    const m = document.getElementById('chatbot-messages');\n" +
		        "    m.scrollTop = m.scrollHeight;\n" +
		        "}\n"+
		        "</script>\n";

		    return script;
		}


	public static String getCss() 
	{
		String css=     //".header .vheader .nav-logo>a .logo{width:200%;}"+
				".dark .header, body.dark {background-color:rgba(36,49,64,0.8);}"+
				".header .vheader .nav-left, .header .vheader .nav-right {padding-left:revert-layer;}"+
				".dark .test-content{border-style:solid;border-color:black;border-width:2px;}"+
				".badge.log{font-size:100%;color:white;padding:5px;}"+
				".pass-bg {background-color:darkgreen;}"+
				".fail-bg{background-color:darkred;}"+
				".badge{border-radius:5px;font-size:105%;}"+
				//".header .vheader .search-input input {width:90%;text-align:left;}"+
				".badge-primary{background-color:rgba(101, 105, 223, 0.7);}"+
				".dark .badge-default{border:1px solid #007bff !important;box-shadow: 2px 2px #6c757d;}"
				;
		return css;
	}

}