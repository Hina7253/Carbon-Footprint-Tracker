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
