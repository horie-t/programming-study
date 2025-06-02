import httpx
import os

from mcp.server.fastmcp import FastMCP

mcp = FastMCP("Terraform Registry")

@mcp.tool()
async def fetch_latest_module_version(namespace: str, name: str, provider: str):
    """Fetch latest module version number"""
    async with httpx.AsyncClient() as client:
        response = await client.get(f"https://registry.terraform.io/v1/modules/{namespace}/{name}/{provider}")
        latest_module = response.json()
        return latest_module["version"]

def main() -> None:
    mcp.run()
