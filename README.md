- deploy on heroku  
  mvn clean package  
  heroku deploy:jar target/telegram-0.0.2-SNAPSHOT.jar --app mentoring-review-bot


- logs  
  heroku logs --tail --app mentoring-review-bot



- manage  
  heroku run bash --app mentoring-review-bot


commands for bot owner only
  -
- /announce - "Отправить аннонс о новых возможностях указанным пользователям"
- /set_contract - "Задать номер договора студента"
- /set_salary - "Установить новое значение зп"
- /step_passed - "Отметить для студента пройденный шаг обучения"
- /set_period - "Установить период времени, за который выводить список ревью"
- /report_delete - "Удалить отчеты студента"
- /register_mentor - "Зарегистрировать чат как менторский"
- /register_report - "Зарегистрировать чат как чат с отчетами"
- /add_mentor - "Добавить ментора"
- /daily - "Назначить ежедневное уведомление"
- /daily_delete - "Удалить все ежедневные уведомление в данном чате"
