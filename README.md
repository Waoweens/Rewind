# Rewind
rewind time, max caulfield style

## Project status
we have packet logging to an SQLite database now!

problems:
- db files get very big very fast (playing for a few seconds got me a 25 MiB file)
  - another packet logging mod, [Ledger](https://github.com/QuiltServerTools/Ledger), got me 500 KiB after a few minutes. need to investigate what they're doing (or just use their classes, it's LGPL after all :3)
- need to add more metadata to packets (e.g. which world they were sent in)
- should also put them in the level directory instead of the root directory