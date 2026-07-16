package fastfilescrape;

final class Chunker {

    private Chunker() {}

    static void chunk(CharSequence content, int maxBytes, ChunkSink sink) {
        int len = content.length();
        int start = 0;
        int chunkIndex = 0;

        while (start < len) {
            int end = approximateEnd(content, start, maxBytes);
            sink.onChunk(chunkIndex++, content.subSequence(start, end));
            start = end;
        }
    }

    private static int approximateEnd(CharSequence s, int start, int maxBytes) {
        int len = s.length();
        int bytes = 0;
        int i = start;
        int lastNewline = -1;

        while (i < len && bytes <= maxBytes) {
            char c = s.charAt(i);
            bytes += (c <= 0x7F) ? 1 : (c <= 0x7FF ? 2 : 3);
            if (c == '\n') lastNewline = i;
            if (bytes > maxBytes) break;
            i++;
        }

        if (lastNewline > start + 100) {
            return lastNewline + 1;
        }
        return Math.min(i, len);
    }

    interface ChunkSink {
        void onChunk(int chunkIndex, CharSequence chunk);
    }
}
