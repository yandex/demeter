import React from 'react';
import {Button, Divider, Menu, Space} from 'antd';
import {DeleteOutlined, DownOutlined, LockOutlined, UnlockOutlined} from '@ant-design/icons';
import {
  DataSource,
  DataTable,
  DataTableColumn,
  PluginClient,
  createDataSource,
  createState,
  usePlugin,
  useValue,
  createTablePlugin
} from 'flipper-plugin';

type Row = {
  id: string;
  ms: number;
  count: number;
  className: string;
  methodName: string;
  thread: string;
  timestamp: string;
};

type Events = {
  newRow: Row;
};

function createColumnConfig(): DataTableColumn<Row>[] { 
  return [
    {
      key: 'ms',
      title: 'Max ms',
      width: 120
    },
    {
      key: 'count',
      title: 'Entries',
      width: 120
    },
    {
      key: 'className',
      title: 'Class Name'
    },
    {
      key: 'methodName',
      title: 'Method Name'
    },
    {
      key: 'thread',
      title: 'Thread'
    },
    {
      key: 'timestamp',
      title: 'Timestamp',
    }
  ]
}

const {plugin, Component} = createTablePlugin<Row>({
  columns: createColumnConfig(),
  method: 'newRow',
  key: 'id'
});
export {plugin, Component};