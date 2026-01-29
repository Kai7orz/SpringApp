import CodeMirror from '@uiw/react-codemirror';
import { sql, MySQL } from '@codemirror/lang-sql';

interface SqlEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  height?: string;
}

export default function SqlEditor({ value, onChange, placeholder, height = '200px' }: SqlEditorProps) {
  return (
    <div className="border rounded-lg overflow-hidden">
      <CodeMirror
        value={value}
        height={height}
        extensions={[sql({ dialect: MySQL })]}
        onChange={onChange}
        placeholder={placeholder || 'Enter your SQL query here...'}
        theme="light"
        className="sql-editor"
        basicSetup={{
          lineNumbers: true,
          highlightActiveLineGutter: true,
          highlightActiveLine: true,
          foldGutter: true,
          autocompletion: true,
        }}
      />
    </div>
  );
}
