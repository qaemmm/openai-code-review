curl --location 'https://open.bigmodel.cn/api/paas/v4/chat/completions' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiZGNhY2RkNWY2OTgyNGY0OTA5ZTI3OGNiM2FjMTc1YTEiLCJleHAiOjE3MzU1Njg5ODkyMTYsInRpbWVzdGFtcCI6MTczNTU2NzE4OTIxN30.y4OushUOPEtGkdj5tcdTwIUsJ76r9A0ZTB7nrlFUDzQ' \
--header 'Content-Type: application/json' \
--data '{
    "model": "glm-4-flash",
    "messages": [
        {
            "role": "user",
            "content": "冒泡排序安排一个"
        }
    ]
}'
