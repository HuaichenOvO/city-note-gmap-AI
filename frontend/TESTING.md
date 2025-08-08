# Frontend Testing Guide

## 测试环境设置

项目使用以下测试工具：
- **Vitest** - 测试运行器
- **React Testing Library** - React组件测试
- **@testing-library/jest-dom** - 额外的DOM匹配器
- **@testing-library/user-event** - 用户交互模拟

## 运行测试

### 运行所有测试
```bash
npm test
```

### 运行特定测试文件
```bash
npm test src/components/__tests__/Login.test.tsx
```

### 运行测试并生成覆盖率报告
```bash
npm run test:coverage
```

### 运行测试UI界面
```bash
npm run test:ui
```

## 测试文件结构

```
src/
├── components/
│   ├── __tests__/
│   │   ├── Login.test.tsx
│   │   ├── Signup.test.tsx
│   │   ├── Note.test.tsx
│   │   └── SearchBar.test.tsx
│   ├── Login.tsx
│   ├── Signup.tsx
│   └── Note.tsx
├── api/
│   ├── __tests__/
│   │   └── authApi.test.ts
│   └── authApi.ts
└── test/
    ├── setup.ts
    ├── test-utils.tsx
    └── simple.test.ts
```

## 测试类型

### 1. 组件测试 (Component Tests)
测试React组件的渲染、用户交互和状态变化。

**示例测试场景：**
- 组件正确渲染
- 用户输入处理
- 事件处理
- 错误状态显示
- 表单验证

### 2. API测试 (API Tests)
测试API调用的成功和失败情况。

**示例测试场景：**
- API调用成功
- API调用失败
- 错误处理
- 数据转换

### 3. 工具函数测试 (Utility Tests)
测试纯函数和工具方法。

## 测试最佳实践

### 1. 测试命名
- 使用描述性的测试名称
- 遵循 "should" 或 "when" 模式
- 测试名称应该说明预期行为

### 2. 测试结构
```typescript
describe('Component Name', () => {
  beforeEach(() => {
    // 设置测试环境
  });

  test('should render correctly', () => {
    // 测试渲染
  });

  test('should handle user interaction', () => {
    // 测试用户交互
  });
});
```

### 3. Mock使用
- 使用 `vi.mock()` 来mock外部依赖
- 使用 `vi.fn()` 创建mock函数
- 在 `beforeEach` 中清理mock状态

### 4. 异步测试
- 使用 `waitFor()` 处理异步操作
- 使用 `async/await` 处理Promise

## 常见问题解决

### 1. Mock问题
如果遇到mock相关错误，确保：
- Mock在导入被测试模块之前定义
- 使用正确的mock语法（Vitest使用 `vi` 而不是 `jest`）

### 2. 组件渲染问题
如果组件需要Provider，确保：
- 在测试中提供必要的Provider
- 使用 `customRender` 函数

### 3. 异步测试问题
如果异步测试失败，确保：
- 使用 `waitFor()` 等待异步操作完成
- 正确设置mock的返回值

## 添加新测试

### 1. 创建测试文件
在对应的 `__tests__` 目录下创建 `.test.tsx` 文件。

### 2. 导入必要依赖
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import YourComponent from '../YourComponent';
```

### 3. 编写测试
```typescript
describe('YourComponent', () => {
  test('should render correctly', () => {
    render(<YourComponent />);
    expect(screen.getByText('Expected Text')).toBeInTheDocument();
  });
});
```

## 测试覆盖率

运行覆盖率测试：
```bash
npm run test:coverage
```

覆盖率报告会显示：
- 语句覆盖率
- 分支覆盖率
- 函数覆盖率
- 行覆盖率

## 持续集成

测试会在CI/CD流程中自动运行，确保：
- 所有测试通过
- 覆盖率达到要求
- 代码质量符合标准 