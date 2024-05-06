import React, {useState, useEffect} from 'react';
import { onValue } from "firebase/database";
import { myData } from './util/firebase';
import { Table } from 'antd';
import 'antd/dist/antd.css';

const Database = () => {
	const [data, setData] = useState();
	
// listening on value changes and setting the snapshots to the state
	useEffect(() => {
		const ref = myData;
		onValue(ref, (snapshot) => {
			const datas = snapshot.val();
			const data = [];
			for (let id in datas){
				data.push({id, ...datas[id]});
			}
			setData(data);
		});
	}, []);	

// creating columns to print as table 
	const columns = [
	        {
	          title: 'ID',
	          dataIndex: 'id',
	        },
	        {
	          title: 'Student',
	          dataIndex: 'user',
	        },
	        {
	          title: 'Incident',
	          dataIndex: 'incident',
	          render : (text) => String(text),

	        },
	        {
	            title: 'PictureURL',
	            dataIndex: 'dataURL',
				render: dataURL => <img style={{height: "300px"}} alt={dataURL} src={`data:image/png;base64,${dataURL}`}/>,
	        },
    ];

// show the updating Database
  return (
     <div>
	  {console.log(JSON.stringify(data))}
	  <Table rowKey="id" bordered={true} dataSource={data} columns={columns} pagination={false} />
	 </div>
  );
}

export default Database;