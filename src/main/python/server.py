import asyncio
import websockets
import json
print(json.dumps({"test": "ok"}))
import httpx

API_APOLLO_KEY = "3QM-PzFzdCAEtJuB12cRAQ"

async def process_prospect(websocket):
    try:
        async for message in websocket:
            data = json.loads(message)
            print(f"📥 Reçu de Kotlin : {data}")

            linkedin_url = data.get("linkedinURL", "")
            email = "email@inconnu.fr"

            if linkedin_url:
                try:
                    email = await get_email(linkedin_url)
                except Exception as e:
                    print(f"⚠ Erreur récupération email : {e}")

            data["name"] = "Nom Inconnu"
            data["email"] = email
            data["status"] = "completed"

            response = json.dumps(data)
            print(f"📤 Envoi vers Kotlin : {response}")
            await websocket.send(response)

    except websockets.exceptions.ConnectionClosed as e:
        print(f"❌ Connexion WebSocket fermée : {e.rcvd.reason}")
    except Exception as e:
        print(f"❌ Erreur WebSocket : {e}")
        await websocket.send(json.dumps({"status": "error", "message": str(e)}))

async def get_email(linkedin_url):
    api_url = "https://api.apollo.io/api/v1/people/match"
    headers = {"Authorization": f"Bearer {API_APOLLO_KEY}", "Content-Type": "application/json"}
    params = {"linkedin_url": linkedin_url, "reveal_personal_emails": True}

    async with httpx.AsyncClient(timeout=10) as client:
        try:
            response = await client.post(api_url, json=params, headers=headers)
            response.raise_for_status()
            data = response.json()
            email = data.get("email") or data.get("emails", ["email@inconnu.fr"])[0]
            print(f"✅ Email trouvé : {email}")
            return email
        except httpx.HTTPStatusError as e:
            print(f"❌ Erreur HTTP {e.response.status_code} : {e.response.text}")
        except httpx.RequestError as e:
            print(f"❌ Erreur de requête : {e}")
        except Exception as e:
            print(f"❌ Erreur inattendue : {e}")

    return "email@inconnu.fr"

async def main():
    print("🔗 Serveur WebSocket en ligne...")
    async with websockets.serve(process_prospect, "0.0.0.0", 9000, max_size=2**20):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
