import asyncio
import websockets
import json
import requests

async def process_prospect(websocket):
    try:
        async for message in websocket:
            data = json.loads(message)
            print(f"📥 Reçu de Kotlin : {data}")

            linkedin_url = data["linkedinURL"]
            try:
                email = get_email(linkedin_url)
            except NameError:
                email = "email@inconnu.fr"

            data["name"] = "Nom Inconnu"
            data["email"] = email
            data["status"] = "completed"

            response = json.dumps(data)
            print(f"📤 Renvoi vers Kotlin : {response}")
            await websocket.send(response)

    except Exception as e:
        print(f"⚠ Erreur lors du traitement du prospect : {e}")
        await websocket.send(json.dumps({"status": "error", "message": str(e)}))

def get_email(linkedin_url):
    api_url = "https://api.apollo.io/v1/people/search"
    headers = {"Authorization": "Bearer 3QM-PzFzdCAEtJuB12cRAQ"}
    params = {"linkedin_url": linkedin_url}
    response = requests.get(api_url, headers=headers, params=params)

    if response.status_code == 200:
        data = response.json()
        return data['email']
    else:
        print(f"Erreur lors de la récupération de l'email : {response.text}")
        return "email@inconnu.fr"


async def main():
    print("🔗 Serveur WebSocket lancé, en attente de connexions...")
    async with websockets.serve(process_prospect, "localhost", 8765):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
