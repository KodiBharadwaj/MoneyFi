# 💰 MoneyFi — Personal Finance Tracker

MoneyFi is a full-stack personal finance management application that helps individuals **track their income, expenses, and savings** to avoid overspending and slipping into debt. With real-time insights, budgeting tools, and data visualization, MoneyFi empowers users to take control of their financial health.

---

## 🚀 Features at a Glance

- 💸 Track incomes and expenses with filters & charts
- 🧾 Generate Excel and PDF reports
- 🎯 Set monthly budgets and savings goals
- 📊 Visual indicators and insights for better decisions
- 🔐 OTP-based signup, secure authentication, jwt authorization
- 🔑 Google Login & GitHub Login (OAuth 2.0 integration)
- 📥 Export/download reports and email them securely
- 📧 Google Email transaction sync (automatic transaction detection from emails)
- 📨 Smart email alerts for transactions, budgets & important activities
- 📄 Account statement generation with password-protected PDF delivery via email
- 🔔 Real-time notifications (in-app) and email notifications
- 🗑️ Soft-delete and recovery of incomes up to 30 days
- 📅 Period-based statements with pagination
- 🔐 Forgot password/username with OTP verification
- 👥 Role-based system: User, Admin & Maintainer
- 🧩 Dynamic email template uploading & management system
---

## 🛠️ Tech Stack

| Layer         | Technology                     |
|---------------|--------------------------------|
| Frontend      | Angular                        |
| Backend       | Java, Spring Boot (Microservices) |
| Database      | Microsoft SQL Server (Stored Procedures, Triggers) |
| DB Backend Interaction | JPA, JPQL JDBC, Java Persistance Context, ORM Mapping |
| Email Service | AWS SES, Spring Mail           |
| File Storage  | AWS S3, Cloudinary             |
| Service Communications  | Kafka, Rest Template |
| Queue Communications | Rabbit MQ, Artemis       |
| Caching | Redis(Distributed), Caffeine(In memory), UI Caching                    |
| Realtime Notifications | Java SSE (Server Side Emitter) |
| Reporting     | Apache POI (Excel), iText (PDF) |
| Cloud         | AWS, GCP                        |
| Security Login  | Spring Security Login, JWT, Google Oauth, Github Oauth |
| Free Sources Used | Render(Backend), Netlify(UI), Free Hosting(DB) |
| Other Tech Used   | Docker, Gemini Api, Eureka, SonarQube, Feign, Git, Github, Postgres, Swagger, Github Packages |
| Tools Used   | Intellij Idea, VS code, SSMS, Postman, PG Admin, Redis Insight, Docker Desktop, Cmd, One Note |

---

## 📸 Screenshots

### 🔐 Landing Page
<img width="1893" height="896" alt="image" src="https://github.com/user-attachments/assets/15af93e1-ac24-4b81-8501-aaf64e2cff4b" />

### Signup
<img width="1891" height="891" alt="Screenshot (7)" src="https://github.com/user-attachments/assets/5b916ae2-4045-4b82-acf7-d27242f1ce23" />

### Login (Email Login, Google Login, Github Login)
<img width="1884" height="898" alt="image" src="https://github.com/user-attachments/assets/cdca8d83-ecd4-49ee-bbbf-ecbca723cd6f" />

### 🔑 OTP Verification (Signup & Forgot Password)
<table> <tr> <td align="center"> <img width="300" alt="Screenshot (12)" src="https://github.com/user-attachments/assets/79b21135-b3f6-4538-8fb4-ebf4606ffd49" /> </td> <td align="center"> <img width="300" alt="Screenshot (14)" src="https://github.com/user-attachments/assets/e49e6608-8250-439f-afa5-9f8ee3c3d7e7" /> </td> </tr> <tr> <td align="center"> <img width="300" alt="Screenshot (17)" src="https://github.com/user-attachments/assets/e9413645-0a93-42de-888a-24a5d5a6e381" /> </td> <td align="center"> <img width="300" alt="Screenshot 2025-07-27 175733" src="https://github.com/user-attachments/assets/506d0597-0b2a-4e46-a41d-a14022822087" /> </td> </tr> </table>

### 🏠 User Dashboard
<img width="1911" height="904" alt="image" src="https://github.com/user-attachments/assets/7a4e8598-785d-43a1-b374-627f9058eb06" />

### 💼 Income Management
- Add, Edit, Delete, Restore (soft delete up to 30 days)
- Filter, Chart View, Excel Export

<img width="1895" height="897" alt="Screenshot (19)" src="https://github.com/user-attachments/assets/329104d4-9c09-456a-a80f-e4fcdd225d82" />


### 💳 Expense Management
- Same features as income: filters, reports, charts, and restore

<img width="1867" height="900" alt="Screenshot (20)" src="https://github.com/user-attachments/assets/c66dd65b-8fae-4c69-8192-84dc03f0f78f" />


### 🧮 Budget Planning
- Create monthly budgets
- Track percentage spent with visual indicators

<img width="1864" height="903" alt="Screenshot (21)" src="https://github.com/user-attachments/assets/c9c8ada9-470b-4e18-a7d2-21eb9b8da84c" />


### 🎯 Savings Goals (Deposits)
- Goal-based saving module

<img width="1862" height="897" alt="Screenshot (22)" src="https://github.com/user-attachments/assets/5a7eba3b-cc93-4baf-a9d1-9945d1d0abd0" />

### AI Analysis and charts
- Spending based charts

<img width="1519" height="759" alt="Screenshot 2025-07-27 180642" src="https://github.com/user-attachments/assets/ba0977aa-fc3d-4b68-8737-f469e80c8ef9" />
<img width="1387" height="896" alt="Screenshot 2025-07-27 180713" src="https://github.com/user-attachments/assets/52fca008-a269-4ffb-b403-72be3bd431c1" />



### 📈 Insights and Statements
- Generate custom statements (daily/monthly/yearly)
- Download PDF or send encrypted via email

<img width="1849" height="891" alt="Screenshot 2025-07-27 180841" src="https://github.com/user-attachments/assets/21b8a889-47ee-4028-8b0c-a5d59070c145" />

### Detect Email Transactions
- Automatic email transactions detection and history with calender

<img width="1893" height="898" alt="image" src="https://github.com/user-attachments/assets/c2bbc71c-83c6-4853-88b0-deb02acc7b88" />

### Real Time Notifications
<img width="1884" height="900" alt="image" src="https://github.com/user-attachments/assets/d4026b9c-426e-47ed-a833-7d470cae7bcb" />

### 👤 Profile Section
- Update profile details
- Upload profile picture to AWS S3

<img width="1523" height="903" alt="Screenshot 2025-07-27 181025" src="https://github.com/user-attachments/assets/4995e1a2-c439-49ad-93df-c5fe10e11084" />


### Account Block, Unblock, retrieval, Name change Request and Tracking 
<img width="1879" height="919" alt="image" src="https://github.com/user-attachments/assets/dbb65481-f0c7-438b-af55-c58da3b398b8" />
<img width="1887" height="897" alt="image" src="https://github.com/user-attachments/assets/b7a341a3-65a1-450e-a042-134093c097d5" />

### Admin Page  
<img width="1897" height="905" alt="image" src="https://github.com/user-attachments/assets/fcd375c7-8257-4f9e-931c-33a9b0478b9b" />


### Maintainer Page  
<img width="1915" height="894" alt="image" src="https://github.com/user-attachments/assets/c1388792-85c7-49a3-bc5a-2e2ec54ebf7d" />

### 🔐 Forgot Username 
- Provide your any relevant details and try fetching

<img width="1894" height="895" alt="Screenshot 2025-07-27 181216" src="https://github.com/user-attachments/assets/25753c88-9500-4acd-a4bc-4bbdc42b7769" />


---
