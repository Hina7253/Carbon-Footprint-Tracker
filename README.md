# 🌱 Digital Carbon Footprint Tracker - Backend

## 📌 Overview
The Digital Carbon Footprint Tracker Backend is developed using Java and Spring Boot.  
It analyzes websites to estimate their environmental impact by calculating energy usage and converting it into CO₂ emissions.

This system helps developers build more sustainable and eco-friendly web applications.

---

## 🚀 Features
- Analyze any website using URL
- Calculate total data transfer (images, videos, scripts, APIs)
- Estimate energy consumption per visit
- Convert energy usage into CO₂ emissions
- Calculate yearly carbon footprint based on traffic
- Identify heavy resources affecting performance
- Provide optimization suggestions

  ---

## 🛠️ Tech Stack
- Java
- Spring Boot
- Maven
- REST APIs
- Postman (for testing)

---

## ⚙️ How It Works
1. User enters a website URL
2. Backend fetches website data
3. Analyzes:
   - Page size
   - Number of requests
   - Media content (images, videos)
   - Scripts and APIs
4. Calculates:
   - Total data size
   - Energy consumption
   - CO₂ emissions
5. Returns analysis and optimization suggestions

---

## 📡 API Endpoints

### 🔹 Analyze Website
**POST** `/api/analyze`

#### Request Body
```json
{
  "url": "https://example.com"
}
```

#### Response
```json
{
  "totalDataSize": "2.5MB",
  "energyConsumption": "0.02 kWh",
  "co2EmissionPerVisit": "0.01 g",
  "yearlyEmission": "3.65 kg",
  "heavyResources": ["images", "videos"],
  "suggestions": [
    "Optimize images",
    "Use lazy loading",
    "Minify CSS/JS"
  ]
}
```
---
## 📂Project Structure
src/
 └── main/
     ├── java/
     │   └── com/project/carbontracker/
     │       ├── controller/
     │       ├── service/
     │       ├── model/
     │       └── repository/
     └── resources/
         └── application.properties

## Getting Started
🔧 Prerequisites
Java 17+
Maven
IDE (IntelliJ / VS Code)

## 📥 Installation
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name

## ▶️ Run the Application
mvn spring-boot:run

## 🧪 Testing
http://localhost:8080/api/analyze

## Use Cases
Developers optimizing website performance
Organizations reducing digital carbon footprint
Green and sustainable tech initiatives

## 📈 Future Enhancements
Real-time monitoring
Dashboard analytics
Browser extension
AI-based optimization suggestions

## 🤝 Contributing
Contributions are welcome! Feel free to fork and submit a pull request.
