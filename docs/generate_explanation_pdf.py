from pathlib import Path

input_path = Path('docs/Explication_Code_Projet.txt')
output_path = Path('docs/Explication_Code_Projet.pdf')

text = input_path.read_text(encoding='utf-8', errors='replace')
raw_lines = text.splitlines()

# Simple word-wrap for PDF text lines
max_chars = 95
lines = []
for line in raw_lines:
    if not line:
        lines.append('')
        continue
    current = ''
    for word in line.split(' '):
        candidate = word if not current else current + ' ' + word
        if len(candidate) <= max_chars:
            current = candidate
        else:
            lines.append(current)
            current = word
    if current:
        lines.append(current)

# Page settings
lines_per_page = 48
pages = [lines[i:i + lines_per_page] for i in range(0, len(lines), lines_per_page)]
if not pages:
    pages = [[]]

objects = []

def add_obj(content_bytes: bytes):
    objects.append(content_bytes)
    return len(objects)

# Reserve object IDs conceptually:
# 1: Catalog, 2: Pages, 3: Font, then per page: page + content
font_obj_id = 3

# Add placeholders for first three objects
add_obj(b'')  # 1
add_obj(b'')  # 2
add_obj(b'')  # 3

page_obj_ids = []
for page_lines in pages:
    text_ops = ['BT', '/F1 10 Tf', '50 790 Td', '14 TL']
    for idx, line in enumerate(page_lines):
        safe = line.replace('\\', r'\\').replace('(', r'\(').replace(')', r'\)')
        if idx == 0:
            text_ops.append(f'({safe}) Tj')
        else:
            text_ops.append('T*')
            text_ops.append(f'({safe}) Tj')
    text_ops.append('ET')
    stream_data = ('\n'.join(text_ops) + '\n').encode('latin-1', errors='replace')

    content_obj_id = add_obj(
        b'<< /Length ' + str(len(stream_data)).encode('ascii') + b' >>\nstream\n' + stream_data + b'endstream\n'
    )
    page_obj_id = add_obj(
        b'<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] '
        b'/Resources << /Font << /F1 3 0 R >> >> '
        b'/Contents ' + str(content_obj_id).encode('ascii') + b' 0 R >>\n'
    )
    page_obj_ids.append(page_obj_id)

# Fill object 1 (Catalog)
objects[0] = b'<< /Type /Catalog /Pages 2 0 R >>\n'

# Fill object 2 (Pages)
kids = ' '.join(f'{pid} 0 R' for pid in page_obj_ids).encode('ascii')
objects[1] = b'<< /Type /Pages /Kids [ ' + kids + b' ] /Count ' + str(len(page_obj_ids)).encode('ascii') + b' >>\n'

# Fill object 3 (Font)
objects[2] = b'<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\n'

# Build final PDF
pdf = bytearray()
pdf.extend(b'%PDF-1.4\n')

offsets = [0]  # object 0 (free)
for i, obj in enumerate(objects, start=1):
    offsets.append(len(pdf))
    pdf.extend(f'{i} 0 obj\n'.encode('ascii'))
    pdf.extend(obj)
    pdf.extend(b'endobj\n')

xref_start = len(pdf)
pdf.extend(f'xref\n0 {len(objects) + 1}\n'.encode('ascii'))
pdf.extend(b'0000000000 65535 f \n')
for off in offsets[1:]:
    pdf.extend(f'{off:010d} 00000 n \n'.encode('ascii'))

pdf.extend(
    b'trailer\n<< /Size ' + str(len(objects) + 1).encode('ascii') + b' /Root 1 0 R >>\n'
    b'startxref\n' + str(xref_start).encode('ascii') + b'\n%%EOF\n'
)

output_path.write_bytes(pdf)
print(f'PDF genere: {output_path}')
