# 🏪 WasteWisePartners

**WasteWisePartners** is the partner/outlet interface of the **WasteWise** mobile ecosystem. It enables businesses, organizations, and eco-partners to contribute to environmental advocacy by creating and managing recycling quests for users of the WasteWise platform.

---

## 🌍 What is WasteWisePartners?

This app empowers outlets to:

- Create recycling-related quests with customizable materials and limits.
- Monitor submissions by players.
- Verify physical item delivery.
- Award EcoPoints to users for successful completions.

It extends the gamified environmental mission of **WasteWise** into real-world community participation and rewards.

---

## 🔐 Access Credentials

Since partner accounts are issued by the developer, here’s an example login:

- **Email:** outlet@gmail.com  
- **Password:** 12345678

> ⚠️ Only whitelisted accounts can access WasteWisePartners. Accounts must be registered and approved by the developer/admin.

---

## ✏️ Quest Creation Features

When creating a quest, the outlet provides:

- 📝 **Title & Description**
- 📦 **Materials Needed**  
  - Dropdown selection of predefined recyclable types.
  - Quantity selection using **NumberPickers**.
  - If **"Other"** is selected, a **TextInput** appears instead.
- 👥 **Maximum Number of Quest Takers**
- 🙈 **Optional Anonymous Commissioner**  
  A commissioner can opt to hide their identity.

### ♻️ Material Points (Criteria)

Each material type corresponds to a base point value:

| Material                  | Points |
|---------------------------|--------|
| Plastic Bottles           | 7      |
| Cans                      | 8      |
| Paper                     | 5      |
| Cardboard                 | 6      |
| Newspaper                 | 4      |
| Other Plastic Materials   | 5      |
| Electronics               | 8      |
| Textiles                  | 8      |
| Intact Glass Item         | 5      |

If the material is not listed and marked as **"Other"**, the app sends the user’s custom input to **Gemini 2.0-Flash** (LLM) which determines the closest matching point value, even if the term doesn’t exactly match the criteria.

### ✨ Quest Points Multiplier

Quests have a **bonus multiplier** applied:  
> `Total Points = (Base Points from Materials) x 2`

This gives incentive for players to prioritize quest participation.

---

## 📲 QR Code & UID Quest Submissions

Partners can **give points** to users in two ways:

1. **QR Code Scan:**  
   Scan the player’s quest QR using the built-in scanner.

2. **Manual UID Entry:**  
   Input the Quest ID (UID) directly if scanning isn’t available.

After submission, the partner physically **verifies the materials** and then confirms to **award EcoPoints** to the user.

---

## ⚙️ Technologies Used

- **Java (Android)**
- **Firebase Authentication**
- **Firebase Firestore**
- **Gemini 2.0-Flash** for smart material classification
- **JourneyApps ZXing QR Scanner**

---

## 🧪 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/iFabsss/WasteWisePartners.git
