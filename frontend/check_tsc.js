const { execSync } = require('child_process');
const fs = require('fs');
try {
    execSync('npx tsc --noEmit --pretty false', { encoding: 'utf8' });
    fs.writeFileSync('tsc_result.txt', 'No errors!\n');
} catch (e) {
    const lines = (e.stdout || '').split('\n').filter(l => l.includes('error TS'));
    const result = lines.map(l => {
        const m = l.match(/^(.+)\((\d+),(\d+)\).*error (TS\d+): (.+)/);
        if (m) return `${m[1]}:${m[2]} ${m[4]}: ${m[5]}`;
        return l;
    }).join('\n');
    fs.writeFileSync('tsc_result.txt', result + '\n' + lines.length + ' errors total\n');
}
