<!-- register.jsp -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>EduStream Hub - Register</title>
    <style>
        /* Inline CSS for register.jsp */
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f6f8;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .register-container {
            width: 100%;
            max-width: 400px;
            background: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            text-align: center;
        }

        h2 {
            margin-bottom: 20px;
            color: #003366;
        }

        .form-group {
            margin-bottom: 15px;
            text-align: left;
        }

        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #333;
        }

        .form-group input[type="text"],
        .form-group input[type="email"],
        .form-group input[type="password"],
        .form-group select {
            width: 100%;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1rem;
            box-sizing: border-box;
        }

        .form-group input[type="submit"] {
            width: 100%;
            padding: 10px;
            background-color: #003366;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            transition: background-color 0.3s;
        }

        .form-group input[type="submit"]:hover {
            background-color: #0055a5;
        }

        p {
            margin-top: 15px;
            font-size: 0.9rem;
        }

        p a {
            color: #0055a5;
            text-decoration: none;
        }

        p a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <h2>Create an Account</h2>
        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label for="name">Full Name:</label>
                <input type="text" name="fullName" id="fullName" required>
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" name="email" id="email" required>
            </div>
            <div class="form-group">
                <label for="role">Role:</label>
                <select name="role" id="role">
                    <option value="TEACHER">Teacher</option>
                    <option value="ADMIN">Administrator</option>
                </select>
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" name="password" id="password" required>
            </div>
            <div class="form-group">
                <input type="submit" value="Register">
            </div>
            <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a></p>
        </form>
    </div>
</body>
</html>
