curl --location 'https://open.bigmodel.cn/api/paas/v4/chat/completions' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiZGNhY2RkNWY2OTgyNGY0OTA5ZTI3OGNiM2FjMTc1YTEiLCJleHAiOjE3MzU1NzA5NDgzNzQsInRpbWVzdGFtcCI6MTczNTU2OTE0ODM4NH0.Oe4_a3OrpScBW42qoDdU93vjHcn97xMgrz69gEmf9vA' \
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
