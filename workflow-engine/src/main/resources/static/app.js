// Workflow Visual Editor - Main Application Logic

class WorkflowEditor {
    constructor() {
        this.nodes = [];
        this.connections = [];
        this.selectedNode = null;
        this.selectedConnection = null;
        this.nodeIdCounter = 1;
        this.scale = 1;
        this.isDragging = false;
        this.draggedNode = null;
        this.isConnecting = false;
        this.connectionStart = null;
        this.tempConnection = null;
        
        this.init();
    }

    init() {
        this.canvas = document.getElementById('nodes-canvas');
        this.svgLayer = document.getElementById('connections-layer');
        this.configPanel = document.getElementById('config-panel');
        this.configContent = document.getElementById('config-content');
        this.executionLog = document.getElementById('execution-log');
        this.logContent = document.getElementById('log-content');
        
        this.setupEventListeners();
        this.setupDragAndDrop();
        this.loadWorkflow();
    }

    setupEventListeners() {
        // Toolbar buttons
        document.getElementById('btn-new').addEventListener('click', () => this.newWorkflow());
        document.getElementById('btn-save').addEventListener('click', () => this.saveWorkflow());
        document.getElementById('btn-load').addEventListener('click', () => this.loadWorkflowDialog());
        document.getElementById('btn-execute').addEventListener('click', () => this.executeWorkflow());
        
        // Zoom controls
        document.getElementById('btn-zoom-in').addEventListener('click', () => this.zoom(0.1));
        document.getElementById('btn-zoom-out').addEventListener('click', () => this.zoom(-0.1));
        
        // Config panel close
        document.getElementById('btn-close-config').addEventListener('click', () => this.closeConfig());
        
        // Log panel close
        document.getElementById('btn-close-log').addEventListener('click', () => this.hideLog());
        
        // Canvas pan
        let isPanning = false;
        let startPan = { x: 0, y: 0 };
        
        this.canvas.addEventListener('mousedown', (e) => {
            if (e.target === this.canvas) {
                isPanning = true;
                startPan = { x: e.clientX, y: e.clientY };
            }
        });
        
        document.addEventListener('mousemove', (e) => {
            if (isPanning) {
                const dx = e.clientX - startPan.x;
                const dy = e.clientY - startPan.y;
                this.panCanvas(dx, dy);
                startPan = { x: e.clientX, y: e.clientY };
            }
        });
        
        document.addEventListener('mouseup', () => {
            isPanning = false;
        });
        
        // Context menu
        document.addEventListener('contextmenu', (e) => this.handleContextMenu(e));
        document.addEventListener('click', () => this.hideContextMenu());
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => this.handleKeyboard(e));
    }

    setupDragAndDrop() {
        // Drag from palette
        document.querySelectorAll('.node-item').forEach(item => {
            item.addEventListener('dragstart', (e) => {
                e.dataTransfer.setData('nodeType', e.target.dataset.type);
            });
        });
        
        // Drop on canvas
        this.canvas.addEventListener('dragover', (e) => {
            e.preventDefault();
            e.dataTransfer.dropEffect = 'copy';
        });
        
        this.canvas.addEventListener('drop', (e) => {
            e.preventDefault();
            const nodeType = e.dataTransfer.getData('nodeType');
            const rect = this.canvas.getBoundingClientRect();
            const x = (e.clientX - rect.left) / this.scale;
            const y = (e.clientY - rect.top) / this.scale;
            this.addNode(nodeType, x, y);
        });
    }

    addNode(type, x, y) {
        const nodeDef = this.getNodeDefinition(type);
        const node = {
            id: `node_${this.nodeIdCounter++}`,
            type: type,
            name: nodeDef.name,
            x: x - 100,
            y: y - 30,
            config: { ...nodeDef.defaultConfig },
            status: 'idle'
        };
        
        this.nodes.push(node);
        this.renderNode(node);
        this.log(`已添加节点：${node.name} (${type})`, 'info');
    }

    getNodeDefinition(type) {
        const definitions = {
            webhook: {
                name: 'Webhook',
                icon: '🌐',
                defaultConfig: {
                    path: '/webhook/' + Math.random().toString(36).substr(2, 9),
                    method: 'POST',
                    headers: {}
                }
            },
            schedule: {
                name: 'Schedule',
                icon: '⏰',
                defaultConfig: {
                    cron: '0 * * * *',
                    timezone: 'UTC'
                }
            },
            httpRequest: {
                name: 'HTTP Request',
                icon: '📡',
                defaultConfig: {
                    url: 'https://api.example.com',
                    method: 'GET',
                    headers: {},
                    body: ''
                }
            },
            code: {
                name: 'Code',
                icon: '💻',
                defaultConfig: {
                    language: 'javascript',
                    code: '// Write your code here\nreturn items;'
                }
            },
            set: {
                name: 'Set',
                icon: '✏️',
                defaultConfig: {
                    assignments: [
                        { key: 'fieldName', value: '{{ $json.value }}' }
                    ]
                }
            },
            switch: {
                name: 'Switch',
                icon: '🔀',
                defaultConfig: {
                    conditions: [
                        { operator: 'equals', value: '', output: 'output1' }
                    ],
                    defaultOutput: 'default'
                }
            },
            merge: {
                name: 'Merge',
                icon: '🔗',
                defaultConfig: {
                    mode: 'append',
                    inputs: 2
                }
            },
            filter: {
                name: 'Filter',
                icon: '🔍',
                defaultConfig: {
                    conditions: [
                        { field: '', operator: 'equals', value: '' }
                    ],
                    logic: 'AND'
                }
            },
            splitInBatches: {
                name: 'Split In Batches',
                icon: '📦',
                defaultConfig: {
                    batchSize: 10,
                    limit: 100
                }
            }
        };
        
        return definitions[type] || { name: type, icon: '❓', defaultConfig: {} };
    }

    renderNode(node) {
        const nodeEl = document.createElement('div');
        nodeEl.className = 'workflow-node';
        nodeEl.id = node.id;
        nodeEl.style.left = `${node.x}px`;
        nodeEl.style.top = `${node.y}px`;
        
        const def = this.getNodeDefinition(node.type);
        
        nodeEl.innerHTML = `
            <div class="connection-point input" data-node="${node.id}" data-type="input"></div>
            <div class="node-header">
                <span class="node-icon">${def.icon}</span>
                <span class="node-name">${node.name}</span>
                <div class="node-actions">
                    <button class="node-action-btn" title="Delete" onclick="editor.deleteNode('${node.id}')">🗑️</button>
                </div>
            </div>
            <div class="node-body">
                <div class="node-status">${node.status}</div>
            </div>
            <div class="connection-point output" data-node="${node.id}" data-type="output"></div>
        `;
        
        // Node selection
        nodeEl.addEventListener('click', (e) => {
            e.stopPropagation();
            this.selectNode(node);
        });
        
        // Node dragging
        const header = nodeEl.querySelector('.node-header');
        header.addEventListener('mousedown', (e) => {
            this.startDragNode(e, node);
        });
        
        // Connection points
        const connectionPoints = nodeEl.querySelectorAll('.connection-point');
        connectionPoints.forEach(point => {
            point.addEventListener('mousedown', (e) => {
                this.startConnection(e, node, point.dataset.type);
            });
        });
        
        this.canvas.appendChild(nodeEl);
    }

    startDragNode(e, node) {
        this.isDragging = true;
        this.draggedNode = node;
        this.dragOffset = {
            x: e.clientX - node.x * this.scale,
            y: e.clientY - node.y * this.scale
        };
        
        const moveHandler = (e) => {
            if (!this.isDragging) return;
            
            const rect = this.canvas.getBoundingClientRect();
            node.x = (e.clientX - this.dragOffset.x) / this.scale;
            node.y = (e.clientY - this.dragOffset.y) / this.scale;
            
            const nodeEl = document.getElementById(node.id);
            nodeEl.style.left = `${node.x}px`;
            nodeEl.style.top = `${node.y}px`;
            
            this.updateConnections();
        };
        
        const upHandler = () => {
            this.isDragging = false;
            this.draggedNode = null;
            document.removeEventListener('mousemove', moveHandler);
            document.removeEventListener('mouseup', upHandler);
        };
        
        document.addEventListener('mousemove', moveHandler);
        document.addEventListener('mouseup', upHandler);
    }

    startConnection(e, node, type) {
        e.stopPropagation();
        this.isConnecting = true;
        this.connectionStart = { node, type };
        
        const startPoint = this.getConnectionPointPosition(node, type);
        
        const moveHandler = (e) => {
            if (!this.isConnecting) return;
            
            const rect = this.svgLayer.getBoundingClientRect();
            const endX = (e.clientX - rect.left) / this.scale;
            const endY = (e.clientY - rect.top) / this.scale;
            
            this.drawTempConnection(startPoint.x, startPoint.y, endX, endY);
        };
        
        const upHandler = (e) => {
            if (!this.isConnecting) return;
            
            const target = e.target.closest('.connection-point');
            if (target && target.dataset.node !== node.id) {
                const targetNode = this.nodes.find(n => n.id === target.dataset.node);
                const targetType = target.dataset.type;
                
                // Validate connection direction
                if ((type === 'output' && targetType === 'input') ||
                    (type === 'input' && targetType === 'output')) {
                    
                    const sourceNode = type === 'output' ? node : targetNode;
                    const targetNodeFinal = type === 'output' ? targetNode : node;
                    
                    this.addConnection(sourceNode, targetNodeFinal);
                }
            }
            
            this.isConnecting = false;
            this.connectionStart = null;
            this.removeTempConnection();
            
            document.removeEventListener('mousemove', moveHandler);
            document.removeEventListener('mouseup', upHandler);
        };
        
        document.addEventListener('mousemove', moveHandler);
        document.addEventListener('mouseup', upHandler);
    }

    getConnectionPointPosition(node, type) {
        const nodeEl = document.getElementById(node.id);
        const rect = nodeEl.getBoundingClientRect();
        const canvasRect = this.canvas.getBoundingClientRect();
        
        return {
            x: type === 'output' ? (rect.right - canvasRect.left) / this.scale : (rect.left - canvasRect.left) / this.scale,
            y: (rect.top + rect.height / 2 - canvasRect.top) / this.scale
        };
    }

    drawTempConnection(x1, y1, x2, y2) {
        this.removeTempConnection();
        
        const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        const d = this.calculateBezierPath(x1, y1, x2, y2);
        path.setAttribute('d', d);
        path.setAttribute('class', 'connection-line');
        path.style.strokeDasharray = '5, 5';
        path.style.opacity = '0.6';
        
        this.svgLayer.appendChild(path);
        this.tempConnection = path;
    }

    removeTempConnection() {
        if (this.tempConnection) {
            this.tempConnection.remove();
            this.tempConnection = null;
        }
    }

    addConnection(source, target) {
        // Check for duplicate
        const exists = this.connections.some(c => 
            c.source === source.id && c.target === target.id
        );
        
        if (exists) {
            this.log('Connection already exists', 'info');
            return;
        }
        
        const connection = {
            id: `conn_${Date.now()}`,
            source: source.id,
            target: target.id
        };
        
        this.connections.push(connection);
        this.renderConnection(connection);
        this.log(`Connected ${source.name} → ${target.name}`, 'info');
    }

    renderConnection(connection) {
        const sourceNode = this.nodes.find(n => n.id === connection.source);
        const targetNode = this.nodes.find(n => n.id === connection.target);
        
        if (!sourceNode || !targetNode) return;
        
        const start = this.getConnectionPointPosition(sourceNode, 'output');
        const end = this.getConnectionPointPosition(targetNode, 'input');
        
        const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        const d = this.calculateBezierPath(start.x, start.y, end.x, end.y);
        path.setAttribute('d', d);
        path.setAttribute('class', 'connection-line');
        path.dataset.connectionId = connection.id;
        
        path.addEventListener('click', (e) => {
            e.stopPropagation();
            this.selectConnection(connection);
        });
        
        this.svgLayer.appendChild(path);
    }

    calculateBezierPath(x1, y1, x2, y2) {
        const deltaX = Math.abs(x2 - x1) * 0.5;
        const cp1x = x1 + deltaX;
        const cp1y = y1;
        const cp2x = x2 - deltaX;
        const cp2y = y2;
        
        return `M ${x1} ${y1} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${x2} ${y2}`;
    }

    updateConnections() {
        // Clear and redraw all connections
        this.svgLayer.innerHTML = '';
        this.connections.forEach(conn => this.renderConnection(conn));
    }

    selectNode(node) {
        // Deselect previous
        if (this.selectedNode) {
            const prevEl = document.getElementById(this.selectedNode.id);
            if (prevEl) prevEl.classList.remove('selected');
        }
        
        this.selectedNode = node;
        const nodeEl = document.getElementById(node.id);
        if (nodeEl) {
            nodeEl.classList.add('selected');
            this.showConfig(node);
        }
    }

    selectConnection(connection) {
        // Deselect previous
        if (this.selectedConnection) {
            const prevEl = this.svgLayer.querySelector(`[data-connection-id="${this.selectedConnection.id}"]`);
            if (prevEl) prevEl.classList.remove('selected');
        }
        
        this.selectedConnection = connection;
        const connEl = this.svgLayer.querySelector(`[data-connection-id="${connection.id}"]`);
        if (connEl) connEl.classList.add('selected');
    }

    showConfig(node) {
        const def = this.getNodeDefinition(node.type);
        
        let configHtml = `
            <div class="form-group">
                <label>节点名称</label>
                <input type="text" id="config-name" value="${node.name}" />
            </div>
        `;
        
        // Generate form fields based on config
        for (const [key, value] of Object.entries(node.config)) {
            if (typeof value === 'object') {
                configHtml += `
                    <div class="form-group">
                        <label>${this.capitalize(key)}</label>
                        <textarea id="config-${key}">${JSON.stringify(value, null, 2)}</textarea>
                    </div>
                `;
            } else if (typeof value === 'boolean') {
                configHtml += `
                    <div class="form-group">
                        <label>${this.capitalize(key)}</label>
                        <select id="config-${key}">
                            <option value="true" ${value ? 'selected' : ''}>是</option>
                            <option value="false" ${!value ? 'selected' : ''}>否</option>
                        </select>
                    </div>
                `;
            } else {
                configHtml += `
                    <div class="form-group">
                        <label>${this.capitalize(key)}</label>
                        <input type="text" id="config-${key}" value="${value}" />
                    </div>
                `;
            }
        }
        
        configHtml += `
            <button class="toolbar-btn primary" style="width: 100%; margin-top: 1rem;" onclick="editor.saveConfig()">保存配置</button>
        `;
        
        this.configContent.innerHTML = configHtml;
        document.getElementById('config-title').textContent = `${def.icon} ${node.name}`;
    }

    saveConfig() {
        if (!this.selectedNode) return;
        
        const nameInput = document.getElementById('config-name');
        if (nameInput) {
            this.selectedNode.name = nameInput.value;
            const nodeEl = document.getElementById(this.selectedNode.id);
            nodeEl.querySelector('.node-name').textContent = this.selectedNode.name;
        }
        
        // Save other config values
        for (const key of Object.keys(this.selectedNode.config)) {
            const input = document.getElementById(`config-${key}`);
            if (input) {
                try {
                    this.selectedNode.config[key] = JSON.parse(input.value);
                } catch {
                    this.selectedNode.config[key] = input.value;
                }
            }
        }
        
        this.log(`${this.selectedNode.name} 配置已保存`, 'success');
    }

    closeConfig() {
        this.configContent.innerHTML = '<p class="placeholder-text">选择一个节点进行配置</p>';
        if (this.selectedNode) {
            const nodeEl = document.getElementById(this.selectedNode.id);
            if (nodeEl) nodeEl.classList.remove('selected');
            this.selectedNode = null;
        }
    }

    deleteNode(nodeId) {
        const nodeIndex = this.nodes.findIndex(n => n.id === nodeId);
        if (nodeIndex === -1) return;
        
        const node = this.nodes[nodeIndex];
        
        // Remove associated connections
        this.connections = this.connections.filter(c => 
            c.source !== nodeId && c.target !== nodeId
        );
        
        // Remove from DOM
        const nodeEl = document.getElementById(nodeId);
        if (nodeEl) nodeEl.remove();
        
        // Remove from array
        this.nodes.splice(nodeIndex, 1);
        
        this.updateConnections();
        this.log(`已删除节点：${node.name}`, 'info');
        
        if (this.selectedNode && this.selectedNode.id === nodeId) {
            this.closeConfig();
        }
    }

    zoom(delta) {
        this.scale = Math.max(0.5, Math.min(2, this.scale + delta));
        document.getElementById('zoom-level').textContent = `${Math.round(this.scale * 100)}%`;
        this.canvas.style.transform = `scale(${this.scale})`;
        this.canvas.style.transformOrigin = 'center center';
        this.updateConnections();
    }

    panCanvas(dx, dy) {
        const currentTransform = this.canvas.style.transform;
        // Simplified pan implementation
    }

    newWorkflow() {
        if (confirm('Create a new workflow? Unsaved changes will be lost.')) {
            this.nodes = [];
            this.connections = [];
            this.canvas.innerHTML = '';
            this.svgLayer.innerHTML = '';
            this.nodeIdCounter = 1;
            this.log('Created new workflow', 'info');
        }
    }

    saveWorkflow() {
        const workflow = {
            nodes: this.nodes,
            connections: this.connections,
            savedAt: new Date().toISOString()
        };
        
        // Save to localStorage as backup
        localStorage.setItem('workflow_current', JSON.stringify(workflow));
        
        // Send to backend
        fetch('/api/workflows', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(workflow)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Save failed');
        })
        .then(data => {
            this.log('工作流已保存到服务器', 'success');
            console.log('Workflow saved:', data);
        })
        .catch(error => {
            this.log('保存到服务器失败：' + error.message, 'error');
            console.error('Error saving workflow:', error);
            // Fallback: still saved to localStorage
            this.log('已保存到本地存储', 'info');
        });
    }

    loadWorkflow() {
        const saved = localStorage.getItem('workflow_current');
        if (saved) {
            const workflow = JSON.parse(saved);
            this.loadWorkflowData(workflow);
            this.log('工作流已加载', 'success');
        }
    }

    loadWorkflowDialog() {
        const saved = localStorage.getItem('workflow_current');
        if (saved) {
            if (confirm('Load saved workflow? Current changes will be lost.')) {
                this.loadWorkflow();
            }
        } else {
            this.log('No saved workflow found', 'info');
        }
    }

    loadWorkflowData(workflow) {
        this.nodes = workflow.nodes || [];
        this.connections = workflow.connections || [];
        
        this.canvas.innerHTML = '';
        this.svgLayer.innerHTML = '';
        
        this.nodes.forEach(node => this.renderNode(node));
        this.connections.forEach(conn => this.renderConnection(conn));
    }

    async executeWorkflow() {
        if (this.nodes.length === 0) {
            this.log('No nodes to execute', 'error');
            return;
        }
        
        this.showLog();
        this.log('Starting workflow execution...', 'info');
        
        // Find trigger nodes
        const triggers = this.nodes.filter(n => ['webhook', 'schedule'].includes(n.type));
        
        if (triggers.length === 0) {
            this.log('未找到触发器节点，请添加 Webhook 或定时触发节点', 'error');
            return;
        }
        
        // Simulate execution
        for (const node of this.nodes) {
            await this.executeNode(node);
        }
        
        this.log('Workflow execution completed', 'success');
    }

    async executeNode(node) {
        const nodeEl = document.getElementById(node.id);
        nodeEl.classList.add('executing');
        node.status = 'executing';
        
        this.log(`Executing: ${node.name}`, 'info');
        
        // Simulate execution delay
        await new Promise(resolve => setTimeout(resolve, 500 + Math.random() * 1000));
        
        node.status = 'success';
        nodeEl.classList.remove('executing');
        nodeEl.classList.add('success');
        
        this.log(`Completed: ${node.name}`, 'success');
        
        setTimeout(() => {
            nodeEl.classList.remove('success');
        }, 1000);
    }

    handleContextMenu(e) {
        e.preventDefault();
        // Could implement context menu here
    }

    hideContextMenu() {
        // Hide context menu
    }

    handleKeyboard(e) {
        if (e.key === 'Delete' && this.selectedNode) {
            this.deleteNode(this.selectedNode.id);
        }
        if (e.key === 'Escape') {
            this.closeConfig();
        }
        if (e.ctrlKey && e.key === 's') {
            e.preventDefault();
            this.saveWorkflow();
        }
    }

    showLog() {
        this.executionLog.classList.add('show');
    }

    hideLog() {
        this.executionLog.classList.remove('show');
    }

    log(message, type = 'info') {
        const entry = document.createElement('div');
        entry.className = `log-entry ${type}`;
        
        const timestamp = new Date().toLocaleTimeString();
        entry.innerHTML = `
            <span class="log-timestamp">${timestamp}</span>
            <span class="log-message">${message}</span>
        `;
        
        this.logContent.appendChild(entry);
        this.logContent.scrollTop = this.logContent.scrollHeight;
    }

    capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }
}

// Initialize editor
let editor;
document.addEventListener('DOMContentLoaded', () => {
    editor = new WorkflowEditor();
});
