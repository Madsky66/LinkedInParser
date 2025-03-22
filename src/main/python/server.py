import asyncio
import websockets
import json
import httpx
from typing import Optional
from dataclasses import dataclass
from bs4 import BeautifulSoup
from google.oauth2 import service_account
from googleapiclient.discovery import build

API_APOLLO_KEY = "3QM-PzFzdCAEtJuB12cRAQ"
SHEETS_CREDENTIALS_FILE = "path/to/credentials.json"
SPREADSHEET_ID = "VOTRE_SPREADSHEET_ID"

@dataclass
class LinkedInProfile:
    url: str
    name: Optional[str] = None
    company: Optional[str] = None
    position: Optional[str] = None
    email: Optional[str] = None

class LinkedInParser:
    def __init__(self):
        self.client = httpx.AsyncClient(timeout=30.0)

    async def parse_profile(self, url: str) -> LinkedInProfile:
        try:
            response = await self.client.get(url)
            soup = BeautifulSoup(response.text, 'html.parser')

            name = self._extract_name(soup)
            company = self._extract_company(soup)
            position = self._extract_position(soup)

            return LinkedInProfile(
                url=url,
                name=name,
                company=company,
                position=position
            )
        except Exception as e:
            print(f"Erreur lors du parsing LinkedIn: {e}")
            return LinkedInProfile(url=url)

    def _extract_name(self, soup) -> Optional[str]:
        # Logique d'extraction du nom
        pass

    def _extract_company(self, soup) -> Optional[str]:
        # Logique d'extraction de l'entreprise
        pass

    def _extract_position(self, soup) -> Optional[str]:
        # Logique d'extraction du poste
        pass

class ApolloAPI:
    def __init__(self, api_key: str):
        self.api_key = api_key
        self.client = httpx.AsyncClient(timeout=30.0)

    async def get_email(self, linkedin_url: str, company: Optional[str] = None) -> Optional[str]:
        try:
            response = await self.client.post(
                "https://api.apollo.io/api/v1/people/match",
                headers={"Authorization": f"Bearer {self.api_key}"},
                json={
                    "linkedin_url": linkedin_url,
                    "organization_name": company,
                    "reveal_personal_emails": True
                }
            )
            data = response.json()
            return data.get("email") or data.get("emails", [None])[0]
        except Exception as e:
            print(f"Erreur Apollo API: {e}")
            return None

class GoogleSheetsManager:
    def __init__(self, credentials_file: str, spreadsheet_id: str):
        credentials = service_account.Credentials.from_service_account_file(
            credentials_file,
            scopes=['https://www.googleapis.com/auth/spreadsheets']
        )
        self.service = build('sheets', 'v4', credentials=credentials)
        self.spreadsheet_id = spreadsheet_id

    async def append_prospect(self, prospect: LinkedInProfile):
        try:
            values = [[
                prospect.name,
                prospect.email,
                prospect.company,
                prospect.position,
                prospect.url,
                "LinkedIn"
            ]]

            self.service.spreadsheets().values().append(
                spreadsheetId=self.spreadsheet_id,
                range='Prospects!A:F',
                valueInputOption='RAW',
                insertDataOption='INSERT_ROWS',
                body={'values': values}
            ).execute()
        except Exception as e:
            print(f"Erreur Google Sheets: {e}")

async def process_prospect(websocket):
    linkedin_parser = LinkedInParser()
    apollo_api = ApolloAPI(API_APOLLO_KEY)
    sheets_manager = GoogleSheetsManager(SHEETS_CREDENTIALS_FILE, SPREADSHEET_ID)

    try:
        async for message in websocket:
            data = json.loads(message)
            linkedin_url = data.get("linkedinURL", "")

            profile = await linkedin_parser.parse_profile(linkedin_url)
            email = await apollo_api.get_email(linkedin_url, profile.company)

            if not email:
                email = f"{profile.name.lower().replace(' ', '.')}@{profile.company.lower()}.com"

            profile.email = email
            await sheets_manager.append_prospect(profile)

            response = {
                "name": profile.name,
                "email": profile.email,
                "company": profile.company,
                "position": profile.position,
                "status": "completed"
            }

            await websocket.send(json.dumps(response))

    except Exception as e:
        print(f"Erreur générale: {e}")
        await websocket.send(json.dumps({
            "status": "error",
            "message": str(e)
        }))

async def main():
    print("🚀 Démarrage du serveur WebSocket...")
    async with websockets.serve(process_prospect, "0.0.0.0", 9000):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
