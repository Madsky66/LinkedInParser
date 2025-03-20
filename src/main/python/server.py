import asyncio
import websockets
import json

async def process_prospect(websocket):
    async for message in websocket:
        data = json.loads(message)
        print(f"📥 Reçu de Kotlin : {data}")

        linkedin_url = data["linkedinURL"]
        try:
            # email = get_email(linkedin_url)
            email = "email@test.com"
        except NameError:
            email = "email@inconnu.fr"

        data["name"] = "Nom Inconnu"
        data["email"] = email
        data["status"] = "completed"

        response = json.dumps(data)
        print(f"📤 Renvoi vers Kotlin : {response}")
        await websocket.send(response)

async def main():
    async with websockets.serve(process_prospect, "localhost", 8765):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())