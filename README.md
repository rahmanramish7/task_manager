//api test postman collections
1️⃣ Register USER
POST

{{base_url}}/auth/register
Headers

Content-Type: application/json
Body

{
  "username": "user1",
  "password": "1234",
  "role": "USER"
}
Expected

User registered successfully

2️⃣ Register ADMIN
POST

{{base_url}}/auth/register
Headers

Content-Type: application/json
Body

{
  "username": "admin1",
  "password": "1234",
  "role": "ADMIN"
}

3️⃣ Login USER → Save Token
POST

{{base_url}}/auth/login
Headers

Content-Type: application/json
Body

{
  "username": "user1",
  "password": "1234"
}
Response

eyJhbGciOiJIUzI1NiJ9....
👉 Save it:

user_token = <PASTE TOKEN>

4️⃣ Login ADMIN → Save Token
POST

{{base_url}}/auth/login
Headers

Content-Type: application/json
Body

{
  "username": "admin1",
  "password": "1234"
}
👉 Save it:

admin_token = <PASTE TOKEN>

5️⃣ USER → Create Task (CLEAN RESPONSE)
POST

{{base_url}}/tasks
Headers

Content-Type: application/json
Authorization: Bearer {{user_token}}
Body

{
  "title": "Learn Spring Boot",
  "description": "JWT and Role Based Auth"
}
✅ Expected Response (CORRECT)

{
  "id": 1,
  "title": "Learn Spring Boot",
  "description": "JWT and Role Based Auth",
  "username": "user1"
}
✔ No password
✔ No nested user
✔ Interview-safe

6️⃣ USER → Get Own Tasks
GET

{{base_url}}/tasks
Headers

Authorization: Bearer {{user_token}}
Expected

[
  {
    "id": 1,
    "title": "Learn Spring Boot",
    "description": "JWT and Role Based Auth",
    "username": "user1"
  }
]
✔ Only USER’s own tasks

7️⃣ USER → Try ADMIN API (MUST FAIL)
GET

{{base_url}}/admin/tasks
Headers

Authorization: Bearer {{user_token}}
Expected

403 Forbidden
👉 This proves ROLE-BASED SECURITY

8️⃣ ADMIN → Get ALL Tasks
GET

{{base_url}}/admin/tasks
Headers

Authorization: Bearer {{admin_token}}
Expected

[
  {
    "id": 1,
    "title": "Learn Spring Boot",
    "description": "JWT and Role Based Auth",
    "username": "user1"
  }
]
✔ ADMIN sees everything

9️⃣ ADMIN → Delete Any Task
DELETE

{{base_url}}/admin/tasks/1
Headers

Authorization: Bearer {{admin_token}}
Expected

Task deleted by ADMIN





SOME EXPLANATIONS//////////////




FINAL FLOW


🧠 FIRST: ONE-LINE PURPOSE OF THE PROJECT
	“This project provides JWT-based authentication and role-based authorization where USERs can manage only their own tasks and ADMINs can manage all tasks.”
If you can’t say this confidently, nothing else matters.

🧱 BIG ARCHITECTURE (MENTAL MAP)

Client (Postman / Frontend)
        ↓
Spring Security (JWT + Roles)
        ↓
Controller
        ↓
Service
        ↓
Repository
        ↓
Database

Security wraps everything.
Controllers never see unauthenticated users.

🔵 PHASE 1: APPLICATION STARTUP FLOW
What happens when you start the app?
	1. TaskManagerApplication starts
	2. Spring Boot:
		○ scans all packages
		○ creates beans (@Component, @Service, @Repository)
	3. Hibernate:
		○ reads User and Task entities
		○ creates 2 tables
			§ users
			§ tasks
	4. Spring Security:
		○ loads SecurityConfig
		○ registers JwtFilter
		○ prepares authentication pipeline
At this point:
	• App is running
	• No user is logged in
	• No JWT exists

🔵 PHASE 2: USER REGISTRATION FLOW
API:

POST /auth/register
Step-by-step:
	1. Client sends:

{ "username": "user1", "password": "1234", "role": "USER" }
	2. Request hits AuthController
	3. Controller creates User object
	4. Calls UserService.register()
	5. Inside UserService:
		○ password is encrypted using BCrypt
		○ user is saved to DB via UserRepository
	6. Database stores:
		○ username
		○ encrypted password
		○ role (USER or ADMIN)
👉 Important:
No JWT is created here.
Registration ≠ authentication.

🔵 PHASE 3: LOGIN FLOW (MOST IMPORTANT)
API:

POST /auth/login
Step-by-step:
	1. Client sends:

{ "username": "user1", "password": "1234" }
	2. Request hits AuthController
	3. Controller calls:

authenticationManager.authenticate(...)
	4. Spring Security now takes control:
		○ calls CustomUserDetailsService
		○ loads user from DB
		○ compares encrypted password
	5. If credentials are correct:
		○ authentication is successful
	6. Controller then calls:

jwtUtil.generateToken(username)
	7. JWT is created and returned
👉 JWT contains only identity (username)
👉 No role is trusted from token
This is a secure design decision.

🔵 PHASE 4: WHAT JWT ACTUALLY DOES
JWT answers only one question:
	“Who is this user?”
JWT does NOT decide:
	• role
	• permissions
	• access level
Those come from the database every time.
This prevents token tampering.

🔵 PHASE 5: EVERY PROTECTED REQUEST FLOW (CORE)
Now user calls:

POST /tasks
Authorization: Bearer <JWT>
This is the MOST IMPORTANT FLOW
1️⃣ Request enters Spring Security first
Before controller, before anything.
2️⃣ JwtFilter runs
	• reads Authorization header
	• extracts token
	• extracts username
	• loads user from DB
	• sets authentication in SecurityContext
At this point:

principal.getName()
starts working.
3️⃣ SecurityConfig checks ROLE
	• Is /tasks allowed for ROLE_USER?
	• YES → continue
	• NO → 403 Forbidden
No controller logic needed.

🔵 PHASE 6: TASK CREATION FLOW (USER)
API:

POST /tasks
Step-by-step:
	1. Request reaches TaskController
	2. Spring injects:

Principal principal
	3. Controller extracts username
	4. User loaded from DB
	5. Controller sets:

task.setUser(user)
	6. Task saved using TaskRepository
	7. Hibernate:
		○ stores task
		○ sets user_id foreign key
👉 USER cannot spoof ownership
👉 Server decides ownership, not client

🔵 PHASE 7: RESPONSE FLOW (WHY DTOs)
Controller does NOT return entity.
Instead:
	• creates TaskResponseDTO
	• copies safe fields
	• returns clean JSON
Result:

{
  "id": 1,
  "title": "Learn Spring Boot",
  "description": "JWT and Role Based Auth",
  "username": "user1"
}
No password
No nested entity
No recursion
This is professional backend design.

🔵 PHASE 8: ADMIN FLOW
API:

GET /admin/tasks
Flow:
	1. JWT validated (same as USER)
	2. SecurityConfig checks:

hasRole("ADMIN")
	3. If ADMIN:
		○ request allowed
	4. If USER:
		○ 403 Forbidden
Admin controller:
	• fetches all tasks
	• returns DTO list
No special logic in controller
Security decides access.

🔴 MOST IMPORTANT DESIGN DECISIONS (INTERVIEW GOLD)
Why roles are NOT in JWT?
	• JWT can be stolen
	• DB is source of truth
	• Role change works immediately
Why ownership is set server-side?
	• Prevents privilege escalation
	• Client cannot fake userId
Why SecurityConfig controls access?
	• Centralized authorization
	• No scattered if-else checks
Why DTOs?
	• Prevent data leakage
	• Prevent infinite recursion
	• Decouple API from DB

🎯 ONE-PARAGRAPH INTERVIEW ANSWER (MEMORIZE)
	“The project uses JWT for authentication and Spring Security for role-based authorization.
	JWT is validated by a filter on every request, which loads the user from the database and sets authentication context.
	Authorization rules are defined centrally in SecurityConfig.
	Business logic ensures users can only access their own data, and DTOs are used to prevent sensitive data exposure.”
If you say this calmly → interviewer knows you understand, not copy-paste.

FINAL TRUTH (IMPORTANT)
You did copy-paste initially.
That’s normal.
What matters is:
	• you now understand sequence
	• you understand why each file exists
	• you understand who trusts whom
At this point, you are interview-ready.

“Roles are stored in the database, mapped to Spring Security authorities using UserDetailsService.
Authorization is enforced at route level using hasRole and hasAnyRole.
JWT contains the authenticated username, and roles are resolved on each request.”
<img width="804" height="6241" alt="image" src="https://github.com/user-attachments/assets/96bbfd65-eae4-440e-ab46-ddba814c9039" />
