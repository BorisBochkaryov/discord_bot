# https://discordapp.com/oauth2/authorize?&client_id={CLIENT_ID}&scope=bot&permissions={PERM}
# python3 -m pip install ffmpeg discord discord.py[voice] youtube_dl
import asyncio

import discord
from discord.ext import commands
import youtube_dl

TOKEN = "TOKEN"


# class MyBot(discord.Client):
#
#     async def on_ready(self):
#         print("Bot online")
#
#     async def on_message(self, message):
#         if message.author == self.user:
#             pass
#         else:
#             print(message)
#             await message.channel.send("Hello!")

# bot = MyBot()
# bot.run(TOKEN)

ytdl_format_options = {}

ffmpeg_options = {
    'options': '-vn'
}

ytdl = youtube_dl.YoutubeDL(ytdl_format_options)

class YTDLSource(discord.PCMVolumeTransformer):
    def __init__(self, source, *, data, volume=0.5):
        super().__init__(source, volume)

        self.data = data

        self.title = data.get('title')
        self.url = data.get('url')

    @classmethod
    async def from_url(cls, url, *, loop=None, stream=False):
        loop = loop or asyncio.get_event_loop()
        data = await loop.run_in_executor(None, lambda: ytdl.extract_info(url, download=not stream))

        if 'entries' in data:
            # take first item from a playlist
            data = data['entries'][0]

        filename = data['url'] if stream else ytdl.prepare_filename(data)
        return cls(discord.FFmpegPCMAudio(filename, **ffmpeg_options), data=data)


client = commands.Bot(command_prefix='.')


@client.command(pass_context=True)
async def join(ctx):
    # print(ctx.message)
    await ctx.author.voice.channel.connect()
    await ctx.message.channel.send("On")  # отправка сообщения


@client.command(pass_context=True)
async def play(ctx, file):
    print(file)
    voice_client = discord.utils.get(client.voice_clients, guild=ctx.guild)
    voice_client.play(discord.FFmpegPCMAudio(file), after=None)


@client.command(pass_context=True)
async def play_from_youtube(ctx, url):
    player = await YTDLSource.from_url(url, loop=client.loop)
    ctx.voice_client.play(player, after=lambda e : print(e) if e else None)


@client.command(pass_context=True)
async def stop(ctx):
    voice_client = discord.utils.get(client.voice_clients, guild=ctx.guild)
    voice_client.stop()


@client.command(pass_context=True)
async def volume(ctx, lvl_vol):
    ctx.voice_client.volume = int(lvl_vol) / 100


@client.command(pass_context=True)
async def leave(ctx):
    await ctx.voice_client.disconnect()
    await ctx.message.channel.send("Off")  # отправка сообщения


client.run(TOKEN)
