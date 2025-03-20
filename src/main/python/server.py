import asyncio
import websockets
import json

async def process_prospect(websocket):
    async for message in websocket:
        data = json.loads(message)
        print(f"📥 Reçu de Kotlin : {data}")

        await asyncio.sleep(2)
        data["name"] = "John Doe"
        data["email"] = "johndoe@example.com"
        data["status"] = "completed"

        response = json.dumps(data)
        print(f"📤 Renvoi vers Kotlin : {response}")
        await websocket.send(response)

async def main():
    async with websockets.serve(process_prospect, "localhost", 8765):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
