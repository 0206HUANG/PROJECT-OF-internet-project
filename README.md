# EduStream Hub

A Spring MVC + JSP web application for managing schools and sharing educational content (images and videos).

## Requirements

- JDK 8 or newer
- Maven 3
- Apache Tomcat 9 (Spring 5 uses the `javax.servlet` API, so Tomcat 10+ is **not** supported)

## Build and run

```bash
mvn clean package
```

Deploy `target/EduStreamHub.war` to Tomcat 9 (copy it into `webapps/`), or import the folder into
Eclipse as an *Existing Maven Project* and run it on a Tomcat 9 server.
Then open <http://localhost:8080/EduStreamHub/>.

## Demo accounts

| Role          | Username  | Password  |
|---------------|-----------|-----------|
| Administrator | `admin`   | `admin`   |
| Teacher       | `teacher` | `teacher` |

New accounts can be created on the registration page; they log in with their email address.

## Features

- Login / logout with role-based pages (administrators and teachers)
- Administrator dashboard with live statistics and recent activities
- School list and "add school" form
- Content upload (images and videos, up to 200 MB) and a content library
- Reports page

## Notes

- All data is kept in memory and is reset when the server restarts.
- Uploaded files are saved to `<java.io.tmpdir>/edustream-uploads` by default.
  Start Tomcat with `-Dedustream.upload.dir=/path/to/folder` to use a different folder.
