# API 清单

## 统一约定

- 所有接口返回统一响应体：
```json
{ "code": 0, "message": "success", "data": { } }
```
  - `code = 0` 成功；非 0 表示业务错误（如记录不存在）。
  - `data` 为具体业务数据，可能为 null。
- 错误由全局异常处理器 `GlobalExceptionHandler` 统一捕获，同样返回上述结构。
- 上传接口使用 `multipart/form-data`，其余 JSON 接口使用 `application/json`。

## 页面入口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/` | 上传页 `upload.html` |
| GET | `/list` | 发票列表页 `list.html` |
| GET | `/stats` | 统计图表页 `stats.html` |

---

## 接口列表

### 1. 上传并识别

```
POST /api/upload
Content-Type: multipart/form-data
参数：file（图片文件）
```

上传图片 → 保存 → 落库 → AI 识别 → 写回，一步闭环。

响应示例（识别成功）：
```json
{
  "code": 0,
  "message": "上传并识别成功",
  "data": {
    "fileId": "abc123...",
    "status": "SUCCESS",
    "isInvoice": true,
    "confidence": 0.92,
    "amount": 128.50,
    "invoiceDate": "2026-10-05",
    "company": "某某餐饮有限公司",
    "category": "DINING"
  }
}
```

`message` 按状态区分：
- `SUCCESS` → "上传并识别成功"
- `PARTIAL` → "上传并识别成功，但部分字段缺失，建议人工核对"
- `NOT_INVOICE` → "上传成功，但这张图可能不是发票，请确认"
- 识别异常 → "上传成功，但 AI 识别失败…请重试或人工核对"

### 2. 重新识别（调试用）

```
POST /api/ai/recognize
参数：fileId（表单参数）
```

根据 fileId 重新调用识别并写回。未找到记录时 `code=1`。

### 3. 发票列表

```
GET /api/invoice/list
```

返回所有发票记录（按 id 倒序，最新在前）。

### 4. 发票详情

```
GET /api/invoice/{id}
```

返回单条记录；不存在时 `code=1`、`message="记录不存在"`。

### 5. 人工修正

```
PUT /api/invoice/{id}
Content-Type: application/json
```

请求体：
```json
{ "amount": 128.50, "invoiceDate": "2026-10-05", "company": "某某公司", "category": "DINING" }
```

> 字段齐全则状态置为 `SUCCESS`；金额/日期/公司任一为空则置为 `PARTIAL`。修正后 `corrected=true`。

### 6. 删除

```
DELETE /api/invoice/{id}
```

删除数据库记录，并尽力删除磁盘上的对应图片文件。

### 7. 分类汇总

```
GET /api/stats/summary
```

返回各分类的金额合计、笔数、占比（只统计有效发票，按金额降序）：
```json
{
  "code": 0, "message": "success",
  "data": [
    { "category": "DINING", "categoryName": "餐饮", "totalAmount": 300.50, "count": 2, "percentage": 62.5 },
    { "category": "TRANSPORT", "categoryName": "交通", "totalAmount": 180.00, "count": 1, "percentage": 37.5 }
  ]
}
```

### 8. 月度汇总

```
GET /api/stats/monthly
```

返回每个月的金额合计、笔数（按月份升序）：
```json
{
  "code": 0, "message": "success",
  "data": [
    { "month": "2026-10", "totalAmount": 480.50, "count": 3 }
  ]
}
```

---

## 枚举值

### 状态 status

| 值 | 含义 |
|---|---|
| `PENDING` | 待识别（上传后、识别前） |
| `SUCCESS` | 识别成功（字段齐全） |
| `PARTIAL` | 部分识别（字段缺失，需人工核对） |
| `NOT_INVOICE` | 可能不是发票 |
| `FAILED` | 识别失败 |

### 分类 category

`DINING` / `TRANSPORT` / `SHOPPING` / `OFFICE` / `RESEARCH` / `LODGING` / `CONFERENCE` / `OTHER`

详见 [分类规则](classification.md)。
