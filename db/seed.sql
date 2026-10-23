-- 初始数据。
-- 题库直接沿用第二次课 quiz-game 的 8 道题，保证前后端数据能对上。
-- 执行：mysql -u root -p quiz_db < db/seed.sql

USE quiz_db;

INSERT INTO t_question (category, content, option_a, option_b, option_c, option_d, answer) VALUES
('HTML BASICS', '在 HTML 中，哪个标签用于创建最大的标题？', '<heading>', '<h6>', '<h1>', '<title>', 2),
('HTML BASICS', '哪个属性可以为图片提供替代文本？', 'src', 'alt', 'href', 'title', 1),
('CSS BASICS', '哪个 CSS 属性用于改变文字颜色？', 'font-style', 'text-decoration', 'color', 'background', 2),
('CSS BASICS', '使用 Flexbox 时，哪个属性可以控制主轴方向？', 'align-items', 'flex-direction', 'justify-content', 'flex-wrap', 1),
('CSS BASICS', '哪个单位相对于根元素的字体大小？', 'px', 'em', 'rem', '%', 2),
('JAVASCRIPT', '使用哪个关键字可以声明一个不会重新赋值的变量？', 'var', 'let', 'const', 'static', 2),
('JAVASCRIPT', '哪个方法可以把字符串转换成整数？', 'parseInt()', 'toString()', 'parseFloat()', 'Number.isInteger()', 0),
('JAVASCRIPT', '哪个 DOM 方法可以根据 id 获取元素？', 'querySelectorAll()', 'getElementById()', 'getElementsByClassName()', 'createElement()', 1);

-- 两个演示账号，密码都是 123456（PBKDF2-HMAC-SHA256，12 万轮，随机盐）
-- 注意：同一个密码，两行的摘要也长得不一样——这就是加盐的意义。
INSERT INTO t_user (username, password_hash, email) VALUES
('admin',  'pbkdf2$120000$7KcBJ/tCzf6JMuR6stH92g==$FedM9AUkvvBdD+2tpK8T3oRs7lzUeeWbcT7C7Kaz/Z8=', 'admin@example.com'),
('lingrui', 'pbkdf2$120000$x1hy3XJKyvMmaBwtpjA6tg==$BMqDwuGmRsim64WQlPDh+JWyOZHANwZiiId4lCSV+1Y=', 'lingrui@example.com');

