curl --location 'https://open.bigmodel.cn/api/paas/v4/chat/completions' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiZDcxMTkyYTczZWQ2NTczYmIxNzUzZjk0MzkwNzA4MmQiLCJleHAiOjE3MzUyMTg4MjIzNjgsInRpbWVzdGFtcCI6MTczNTIxNzAyMjM4M30.U0i96u-Pd1DtxXnObHIibgvOWGWTOF9Cz-R4FeUdfWk' \
--header 'Content-Type: application/json' \
--data '{
    "model": "glm-4",
    "messages": [
        {
            "role": "user",
            "content": "冒泡排序安排一个"
        }
    ]
}'
