## **Trivia**
No real email confirmation, just format validation, so write anything with @gmail.com etc

## **How to run the project**
1. Clone the repository. Have Docker Desktop running
2. Proceed to `/Microservices` and write your MySQL local creds info
3. Open terminal in `/Microservices` and call `docker-compose up --build`
4. Open terminal in `/Frontend/kanban-ui` and call `ng serve` 

## **Main flow**
1. Register 2 users
2. Login as 1 of users 
3. Try Create boards / columns / tasks. Try edit, move, delete.
4. Add collaborator (other user) in specific board.
5. Open browser in `New incognito window` login as other user.
6. This user should see a board as collaborator.
7. Try to add new tasks, make edits, move and delete. See how both user have board data auto-update via websokcets
