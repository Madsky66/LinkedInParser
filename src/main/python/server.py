import json
import time
import websockets

async def process_prospect(websocket):
    async for message in websocket:
        data = json.loads(message)
        print(f"📥 Reçu de Kotlin : {data}")

        time.sleep(2)
        data["name"] = "John Doe"
        data["email"] = "johndoe@example.com"
        data["status"] = "completed"

        response = json.dumps(data)
        print(f"📤 Renvoi vers Kotlin : {response}")
        await websocket.send(response)
        print("📤 Résultats envoyés")

async def main():
    async with websockets.serve(process_prospect, "localhost", 8765):
        await asyncio.Future()

if __name__ == "__main__":
    import asyncio
    asyncio.run(main())
