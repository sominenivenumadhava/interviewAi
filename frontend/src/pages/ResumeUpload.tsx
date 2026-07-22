import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Upload, FileText, CheckCircle2, X, AlertCircle } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  CardFooter } from
'../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { mockResume } from '../data/mockData';
export function ResumeUpload() {
  const navigate = useNavigate();
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploaded, setUploaded] = useState(true); // Default to true for demo purposes
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };
  const handleDragLeave = () => {
    setIsDragging(false);
  };
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    simulateUpload();
  };
  const simulateUpload = () => {
    setIsUploading(true);
    setTimeout(() => {
      setIsUploading(false);
      setUploaded(true);
    }, 1500);
  };
  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Resume Context
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          Upload your latest resume. Our AI will analyze it to personalize your
          interview questions.
        </p>
      </div>

      {!uploaded ?
      <Card>
          <CardContent className="p-12">
            <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            className={`flex flex-col items-center justify-center rounded-2xl border-2 border-dashed p-12 text-center transition-colors ${isDragging ? 'border-brand-500 bg-brand-50 dark:bg-brand-900/10' : 'border-ink-200 hover:bg-ink-50 dark:border-ink-700 dark:hover:bg-ink-800/50'}`}>
            
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-brand-100 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400">
                <Upload size={32} />
              </div>
              <h3 className="mb-2 text-lg font-semibold text-ink-900 dark:text-white">
                Drag & drop your resume
              </h3>
              <p className="mb-6 text-sm text-ink-500 dark:text-ink-400">
                Supports PDF, DOCX (Max 5MB)
              </p>
              <Button onClick={simulateUpload} isLoading={isUploading}>
                Browse Files
              </Button>
            </div>
          </CardContent>
        </Card> :

      <motion.div
        initial={{
          opacity: 0,
          y: 20
        }}
        animate={{
          opacity: 1,
          y: 0
        }}
        className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-100 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400">
                    <FileText size={24} />
                  </div>
                  <div>
                    <CardTitle>{mockResume.fileName}</CardTitle>
                    <CardDescription>
                      Uploaded successfully • Parsed by AI
                    </CardDescription>
                  </div>
                </div>
                <Button
                variant="ghost"
                size="icon"
                onClick={() => setUploaded(false)}>
                
                  <X size={20} />
                </Button>
              </CardHeader>
              <CardContent>
                <div className="rounded-xl border border-ink-200 bg-ink-50 p-4 dark:border-ink-800 dark:bg-ink-900/50">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="font-semibold text-ink-900 dark:text-white">
                      ATS Score
                    </h4>
                    <Badge variant="success" className="text-sm px-3 py-1">
                      {mockResume.score} / 100
                    </Badge>
                  </div>
                  <div className="h-2 w-full overflow-hidden rounded-full bg-ink-200 dark:bg-ink-700">
                    <div
                    className="h-full bg-success"
                    style={{
                      width: `${mockResume.score}%`
                    }} />
                  
                  </div>
                  <p className="mt-3 text-sm text-ink-600 dark:text-ink-400">
                    Your resume is well-structured and highlights key technical
                    skills effectively.
                  </p>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Extracted Experience</CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                {mockResume.detectedExperience.map((exp, i) =>
              <div key={i} className="flex gap-4">
                    <div className="mt-1 flex h-2 w-2 shrink-0 rounded-full bg-brand-500" />
                    <div>
                      <h4 className="font-medium text-ink-900 dark:text-white">
                        {exp.role}
                      </h4>
                      <p className="text-sm text-ink-500 dark:text-ink-400">
                        {exp.company} • {exp.duration}
                      </p>
                    </div>
                  </div>
              )}
              </CardContent>
            </Card>
          </div>

          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Detected Skills</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {mockResume.extractedSkills.map((skill, i) =>
                <Badge key={i} variant="secondary">
                      {skill}
                    </Badge>
                )}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Detected Projects</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {mockResume.detectedProjects.map((proj, i) =>
              <div
                key={i}
                className="rounded-lg border border-ink-100 p-3 dark:border-ink-800">
                
                    <h4 className="text-sm font-medium text-ink-900 dark:text-white">
                      {proj.name}
                    </h4>
                    <p className="mt-1 text-xs text-ink-500 dark:text-ink-400">
                      {proj.tech}
                    </p>
                  </div>
              )}
              </CardContent>
            </Card>

            <Button
            className="w-full gap-2"
            onClick={() => navigate('/interview/company')}>
            
              Continue to Interview Setup
            </Button>
          </div>
        </motion.div>
      }
    </div>);

}